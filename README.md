# lfind

- Link to features: https://www.notion.so/CSCE-670-ISR-Project-Lfind-d4a883e31ec6465aad858e2a11929c5c?pvs=4
- command to run the built jar: `java -cp ".\lfind-1.0-SNAPSHOT.jar;C:\Users\vva\.m2\repository\info\picocli\picocli\4.7.5\picocli-4.7.5.jar" cli.LFind "word"`
- check the possibility of RAMDirectory
- popularity based indexing: popular items will be indexed with more granularity
- case sensitive and case in-sensitive searching
- `native-image -cp C:\Users\vva\.m2\repository\info\picocli\picocli\4.7.5\picocli-4.7.5.jar --static -jar .\lfind-1.0-SNAPSHOT.jar`
- `native-image -cp C:\Users\vva\.m2\repository\info\picocli\picocli\4.7.5\picocli-4.7.5.jar -jar .\lfind-1.0-SNAPSHOT.jar`
- `native-image --no-server -cp lfind-1.0-SNAPSHOT.jar --initialize-at-build-time=org.apache.logging.slf4j.SLF4JLogger --initialize-at-run-time=org.slf4j.LoggerFactory -H:Name=israpp Main`