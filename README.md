# MuRa
[![](https://github.com/ZhongXiLu/MuRa/workflows/Maven%20CI/badge.svg)](https://github.com/ZhongXiLu/MuRa/actions?query=workflow%3A%22Maven+CI%22)

Ranks survived mutants according to their importance.

An online example of the generated report can be found [here](example.html) based on [feign](https://github.com/OpenFeign/feign).

## How to Install

The project is built using Maven, so the regular Maven commands apply :)
Use `mvn install` to install the modules.

Alternatively, you can download the jar executable on [the package page](https://github.com/ZhongXiLu/MuRa/packages/222793).

## How to Use

### Using [PITest](http://pitest.org/)

1. First let PITest run the mutation analysis and generate the reports,
make sure there's a directory `/target/pit-reports` present afterwards.
```bash
mvn test -Dfeatures=+EXPORT org.pitest:pitest-maven:mutationCoverage
```
To include PITest into your project, add the following plugin in your `pom.xml`:

```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.4.7</version>
    <configuration>
        <outputFormats>XML</outputFormats>
    </configuration>
    <executions>
        <execution>
            <id>pitest</id>
            <goals>
                <goal>mutationCoverage</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

2. Set up the [config file](https://github.com/ZhongXiLu/MuRa/blob/master/config.xml) for MuRa. Make sure everything is set properly.

3. Now everything is ready to call MuRa (after `mvn install` the executable should be in `pitest/target/pitest-1.0.jar`):
```bash
java -jar pitest-1.1.jar -c config.xml
```

4. Afterwards, the report should be generated in `/index.html`.

5. If you want to find the optimal weights for your project, checkout this [section](https://github.com/ZhongXiLu/MuRa/blob/master/study/README.md).

### Using another mutation testing tool

Use the [`com.github.mura.core`](https://github.com/ZhongXiLu/MuRa/packages/222792) library to call MuRa and pass the survived mutants of your mutation testing tool. An example of how this is done can be found in the [`pitest` module](https://github.com/ZhongXiLu/MuRa/blob/master/pitest/src/main/java/pitest/PITest.java).
