<!--
Copyright (c) KMG. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
-->
# How to build SBK docker images

Run the below command to generate the docker image:

docker build -f ./[File name] ./../ --tag [Image name]


Example:

```
docker build -f ./sbk-file ./../ --tag file-docker

```

Example to build specific version of SBK
```
docker build -f ./dockers/sbk ./ --tag kmgowda/sbk:5.2
```

Example to run:

```
docker run file-docker  -writers 1 -size 100 -seconds 120 -time ns

```

Example to pull specific version of SBK 
```
docker pull kmgowda/sbk:5.2
```

Example to push to repo ; make sure that you login to docker hub before pushing the image
```
docker push kmgowda/sbk:5.2
```