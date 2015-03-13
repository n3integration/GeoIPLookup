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

import java.io.File
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicReference

import com.google.common.base.{Strings, Splitter}
import com.google.common.collect.{RangeMap, TreeRangeMap}
import org.n3integration.lookup.geoip.App._

import scala.annotation.tailrec
import scala.io.Source

class GeoIPDataStore(cacheDir: String) {

  import java.lang.{Long => JavaLong}

  import com.google.common.collect.{Range => GuavaRange}
  import cronish.dsl._

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent._

  private val cache = new AtomicReference[RangeMap[JavaLong, IPCountry]](createCache())

  if (new File(cacheDir, GeoIPDataStore.GEO_FILE).exists()) {
    log.info("GeoIP data cache exists")
    loadCache() onSuccess {
      case _ => log.info("GeoIP data cache loaded")
    }
  }
  else {
    refresh()
  }

  val update = task {
    refresh()
  }

  update executes "every 1st Tuesday in every month"

  def lookup(ip: String) = {
    val ipToLong = IPv4Address.toLong(ip)
    cache.get().get(ipToLong) match {
      case c: IPCountry => Some(c)
      case _ => None
    }
  }

  private def loadCache() = future {
    log.info("Loading GeoIP data cache...")
    val localCache: RangeMap[JavaLong, IPCountry] = createCache()

    for (line <- Source.fromFile(new File(cacheDir, GeoIPDataStore.GEO_FILE)).getLines()) {
      val tokens = Splitter.on(",").trimResults().splitToList(line)
      val low = new JavaLong(stripQuotes(tokens.get(2)).toLong)
      val high = new JavaLong(stripQuotes(tokens.get(3)).toLong)
      val range = GuavaRange.closed(low, high)

      localCache.put(range, new IPCountry(stripQuotes(tokens.get(4)), stripQuotes(tokens.get(5))))
    }
    swapCache(localCache)
  }

  @tailrec
  private def swapCache(localCache: RangeMap[JavaLong, IPCountry]):Unit = {
    val current = cache.get()
    if(!cache.compareAndSet(current, localCache)) {
      swapCache(localCache)
    }
  }

  private def createCache() = {
    val localCache: RangeMap[JavaLong, IPCountry] = TreeRangeMap.create()
    val longVal = new JavaLong(IPv4Address.toLong(IPv4Address.LOCAL))
    val range = GuavaRange.closed(longVal, longVal)
    localCache.put(range, new IPCountry("H2", "Home"))
    localCache
  }

  private def refresh() = {
    log.info("Downloading GeoIP cache data...")

    IO.downloadAndExtract(GeoIPDataStore.GEO_URL, cacheDir) onSuccess {
      case _ => loadCache()
    }
  }

  private def stripQuotes(txt: String) = txt.replaceAll( """"""", "")

  implicit def longToJavaLong(value: Long) = new JavaLong(value)
}

object GeoIPDataStore {
  val GEO_FILE = "GeoIPCountryWhois.csv"
  val CACHE_DIR = cacheDir()
  val GEO_URL = "http://geolite.maxmind.com/download/geoip/database/GeoIPCountryCSV.zip"

  def apply():GeoIPDataStore = this(System.getenv("CACHE_DIR") || CACHE_DIR)
  def apply(cacheDir: String) = new GeoIPDataStore(cacheDir)

  private def cacheDir() = {
    Strings.isNullOrEmpty(System.getenv("CACHE_DIR")) match {
      case true => "/usr/local/share/geoip"
      case false => System.getenv("CACHE_DIR")
    }
  }
}

case class IPCountry(code: String, name: String)

case class IPDetails(address: String, longValue: Long, country: IPCountry)

object IPv4Address {

  val LOCAL = "127.0.0.1"
  val IPv4_PATTERN = """^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$""".r

  def isValid(value: String) = {
    value match {
      case IPv4_PATTERN(_*) => true
      case _ => false
    }
  }

  def toLong(value: String) = {
    value.split('.').zipWithIndex.foldLeft(0l)((acc, v) => {
      acc + (v._1.toInt * math.pow(256, 3 - v._2.toInt).toLong)
    })
  }
}
