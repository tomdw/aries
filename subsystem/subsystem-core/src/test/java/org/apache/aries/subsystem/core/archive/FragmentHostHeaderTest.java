/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.aries.subsystem.core.archive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.aries.subsystem.core.capabilityset.CapabilitySet;
import org.apache.aries.subsystem.core.capabilityset.SimpleFilter;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Namespace;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;

public class FragmentHostHeaderTest {
	@Test
	public void testNullClause() {
		String headerStr = null;
		try {
		    new FragmentHostHeader(headerStr);
		    fail("Null clause not allowed");
		}
		catch (NullPointerException e) {}
		catch (Exception e) {
		    fail("Null clause should result in NPE");
		}
	}
	
	@Test
    public void testEmptyClause() {
        String headerStr = "";
        try {
            new FragmentHostHeader(headerStr);
            fail("Empty clause not allowed");
        }
        catch (IllegalArgumentException e) {}
        catch (Exception e) {
            fail("Empty clause should result in IAE");
        }
    }
	
	@Test
    public void testMultipleClauses() {
        String headerStr = "foo;bundle-version=1.0,bar";
        try {
            new FragmentHostHeader(headerStr);
            fail("Multiple clauses not allowed");
        }
        catch (IllegalArgumentException e) {}
        catch (Exception e) {
            fail("Multiple cluases should result in IAE");
        }
    }
	
	@Test
    public void testSymbolicName() {
        String headerStr = "org.foo";
        FragmentHostHeader header = new FragmentHostHeader(headerStr);
        assertClauses(header, 1);
        assertSymbolicName(header.getClauses().iterator().next(), headerStr);
        assertBundleVersionAttribute(
                header.getClauses().iterator().next(), 
                new VersionRange(VersionRange.LEFT_CLOSED, new Version("0"), null, VersionRange.RIGHT_OPEN));
    }
	
	@Test
	public void testBundleVersionSingle() {
		String headerStr = "com.bar.foo;bundle-version=1.0";
		FragmentHostHeader header = new FragmentHostHeader(headerStr);
		assertClauses(header, 1);
		assertSymbolicName(header.getClauses().iterator().next(), "com.bar.foo");
		assertBundleVersionAttribute(
                header.getClauses().iterator().next(), 
                new VersionRange("1.0"));
	}
	
	@Test
    public void testBundleVersionRange() {
        String headerStr = "com.acme.support;bundle-version=\"[2.0,3.0)\"";
        FragmentHostHeader header = new FragmentHostHeader(headerStr);
        assertClauses(header, 1);
        assertSymbolicName(header.getClauses().iterator().next(), "com.acme.support");
        assertBundleVersionAttribute(
                header.getClauses().iterator().next(), 
                new VersionRange(VersionRange.LEFT_CLOSED, new Version("2.0"), new Version("3.0"), VersionRange.RIGHT_OPEN));
    }
	
	@Test
	public void shouldResolveFragmentHostWithBundleVersionToVersionRangeAttributeAndMatchWithBundlesInThatRange() {
		FragmentHostHeader header = new FragmentHostHeader("Fragment-Host: host-bundle;bundle-version=\"[9.6.0,10)\"");
		assertClauses(header, 1);
        assertSymbolicName(header.getClauses().iterator().next(), "host-bundle");
        assertBundleVersionAttribute(
                header.getClauses().iterator().next(), 
                new VersionRange(VersionRange.LEFT_CLOSED, new Version("9.6.0"), new Version("10"), VersionRange.RIGHT_OPEN));
		FragmentHostRequirement requirement = new FragmentHostRequirement(header.getClauses().iterator().next(), null);
		Resource dummyResource = new Resource() {
			
			@Override
			public List<Requirement> getRequirements(String namespace) {
				return new ArrayList();
			}
			
			@Override
			public List<Capability> getCapabilities(String namespace) {
				return new ArrayList();
			}
		};
		FragmentHostCapability capability = new FragmentHostCapability(new BundleSymbolicNameHeader("host-bundle"), new BundleVersionHeader("9.6.1"), dummyResource);
		dummyResource.getCapabilities("").add(capability);
		
		
		
		String filterDirective = requirement.getDirectives().get(Namespace.REQUIREMENT_FILTER_DIRECTIVE);
        SimpleFilter simpleFilter;
        if (filterDirective == null) {
            simpleFilter = new SimpleFilter(null, null, SimpleFilter.MATCH_ALL);
        }
        else {
            simpleFilter = SimpleFilter.parse(filterDirective);
        }
        String namespace = requirement.getNamespace();
        CapabilitySet capabilitySet = new CapabilitySet(Arrays.asList(HostNamespace.HOST_NAMESPACE), true);
        capabilitySet.addCapability(capability);
        Set<Capability> capabilities = capabilitySet.match(
                simpleFilter, 
                PackageNamespace.PACKAGE_NAMESPACE.equals(namespace)
                        || BundleNamespace.BUNDLE_NAMESPACE.equals(namespace)
                        || HostNamespace.HOST_NAMESPACE.equals(namespace));
        Assert.assertTrue(capabilities.size() == 1);
	}
	
	private void assertBundleVersionAttribute(FragmentHostHeader.Clause clause, VersionRange value) {
	    assertEquals("Wrong bundle version", value, ((BundleVersionAttribute)clause.getAttribute(BundleVersionAttribute.NAME)).getVersionRange());
	}
	
	private void assertClauses(FragmentHostHeader header, int expectedClauses) {
		assertEquals("Wrong number of clauses", expectedClauses, header.getClauses().size());
	}
	
	private void assertSymbolicName(FragmentHostHeader.Clause clause, String value) {
	    assertEquals("Wrong symbolic name", value, clause.getSymbolicName());
	}
}
