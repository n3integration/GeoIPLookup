# GeoIPLookup

A micro web service that provides a lookup from an IPv4 address to its country of origin,
if applicable; otherwise, an appropriate response is returned (e.g. `400`, `404`).

#### Request
```bash
$ curl http://localhost:7070/1.0.0.1
```

#### Success
```json
{
  "status": "ok",
  "response": {
    "ip": "1.0.0.1",
    "country": {
      "code": "AU",
      "name": "Australia"
    }
  }
}
```

#### Error
```json
{
  "status": "error",
  "message": "..."
}
```

### Runs the app on port 7070

    sbt run

### Packaging (fatjar)

    sbt assembly

### To run anywhere else

    java -jar target/*-0.0.1-SNAPSHOT.jar

#### Attribution

* This product includes GeoLite data created by MaxMind, available
from [http://www.maxmind.com](http://www.maxmind.com).

#### License

   Copyright 2015 n3integration

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
