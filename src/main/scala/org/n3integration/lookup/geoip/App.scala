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

import com.twitter.finatra._

object App extends FinatraServer {

  class GeoIPLookupApp extends Controller {

    private val dataStore = GeoIPDataStore()

    get("/:ip?") { request =>
      stats.counter("ipLookup").incr
      val ip = request.routeParams.getOrElse("ip", request.remoteAddress.toString)

      IPv4Address.isValid(ip) match {
        case true => {
          dataStore.lookup(ip)
            .fold(throw new NotFound)((c) => render.json(
              Map("status" -> "ok", "response" -> Map("ip" -> ip, "country" -> c))).toFuture)
        }
        case false => throw new InvalidAddress
      }
    }

    error { request =>
      request.error match {
        case Some(e: InvalidAddress) =>
          render.status(400).json(Map("status" -> "error", "message" -> "invalid IPv4 address")).toFuture
        case Some(e: NotFound) =>
          render.status(404).json(Map("status" -> "error", "message" -> "not found")).toFuture
        case _ =>
          render.status(500).json(Map("status" -> "error", "message" -> "dagger! an unknown error occurred")).toFuture
      }
    }
  }

  class InvalidAddress extends Exception

  class NotFound extends Exception

  register(new GeoIPLookupApp())
}
