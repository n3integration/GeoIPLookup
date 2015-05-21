/*
 *  Copyright 2015 n3integration
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.n3integration.lookup.geoip

import java.io.FileOutputStream

import com.google.common.io.Resources
import org.scalatest.FlatSpec
import org.scalatest.matchers.ClassicMatchers

class GeoIPDataStoreSpec extends FlatSpec with ClassicMatchers {

  Resources.copy(getClass getResource("/GeoIPCountryWhois.csv"), new FileOutputStream("/tmp/GeoIPCountryWhois.csv"))

  val dataStore = GeoIPDataStore("/tmp")

  "The GeoIPDataStore" should "respond with home" in {
    dataStore.lookup(IPv4Address.LOCAL)
      .fold(fail("Unable to find local"))((country) => (assert(country.name.contains("Home"))))
  }

  it should "contain AU" in {
    dataStore.lookup("1.0.0.1")
      .fold(fail("Unable to find match for country"))((country) => (assertResult("AU")(country.code)))
  }

  it should "not contain private addresses" in {
    dataStore.lookup("192.168.1.2")
      .fold()((country) => fail(s"Incorrect response: ${country.code}"))
  }
}
