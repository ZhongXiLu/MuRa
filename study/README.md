## How to get optimal weights

**Important**: to learn the optimal weights, your projects should have existing issues.

1. First build the `study-1.1.jar` executable using `mvn install`.
2. Go to the directory of your project.
3. Set up the [config file](https://github.com/ZhongXiLu/MuRa/blob/master/config.xml) for MuRa.
4. Call the executable
```bash
usage: MuRa
 -c,--config <arg>      Configuration file
 -l,--label <arg>       The label on GitHub for an issue that identifies a
                        bug
 -m,--mutants <arg>     PITest output directory (usually this is
                        /target/pit-reports)
 -o,--owner <arg>       The name of the owner of the repository
 -r,--repo <arg>        The name of the repository
 -s,--submodule <arg>   In case of multimodule, specify the submodule
```
For example, for the core module of [feign](https://github.com/OpenFeign/feign), this is:
```bash
java -cp <> study.Study -c config.xml -m core/target/pit-reports/ -o OpenFeign -r feign -s core
```

5. This will export all the mutants for each bug report to the `/export` directory.
6. To find the optimal weights for this specific project, execute the Python script in `/study/main.py` and pass the generated `export` directory:
```bash
python main.py export
```
7. The optimal weights will be printed out in the terminal. Multiple executions will be ran, so it is possible that there is no general consensus on the weights.

**Important**: the steps above can be repeated for an arbitrary amount of projects to find general weights. To do so, put all the exported files in one directory and pass this directory to the Python script.
