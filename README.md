
Compile without tests!

mvn clean install -DskipTests

Or generate runnable jar

mvn clean compile assembly:single

usage:

 java -jar target/fxstream-tester.jar src/test/resources/stream/all.easybgp src/test/resources/stream/test1.csv "csv.headers=true" test1-out.csv


