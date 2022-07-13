package com.eficode.devstack.container.impl

import com.eficode.devstack.container.ContainerManager
import de.gesellix.docker.client.EngineResponseContent
import de.gesellix.docker.remote.api.ContainerCreateRequest
import de.gesellix.docker.remote.api.HostConfig
import de.gesellix.docker.remote.api.PortBinding

class JsmContainer implements ContainerManager{

    String containerName = "JSM-H2"
    String containerMainPort = "8080"
    long jvmMaxRam = 6000

    JsmContainer() {}

    /**
     * Setup a secure connection to a remote docker
     * @param dockerHost  ex: https://docker.domain.com:2376
     * @param dockerCertPath ex: src/test/resources/dockerCert
     */
    JsmContainer(String dockerHost, String dockerCertPath) {
        dockerClient = setupSecureRemoteConnection(dockerHost, dockerCertPath)
    }


    String createContainer() {

        containerId = createJsmContainer(this.containerName)
        return containerId

    }

    String createJsmContainer(String jsmContainerName = containerName, String imageName = "atlassian/jira-servicemanagement", String imageTag = "latest", long jsmMaxRamMB = jvmMaxRam, String jsmPort = containerMainPort) {

        assert dockerClient.ping().content as String == "OK", "Error Connecting to docker service"


        ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest().tap { c ->

            c.image = imageName + ":" + imageTag
            c.env = ["JVM_MAXIMUM_MEMORY=" + jsmMaxRamMB.toString() + "m", "JVM_MINIMUM_MEMORY=" + ((jsmMaxRamMB / 2) as String) + "m"]
            c.exposedPorts = [(jsmPort + "/tcp"): [:]]
            c.hostConfig = new HostConfig().tap { h -> h.portBindings = [(jsmPort + "/tcp"): [new PortBinding("0.0.0.0", (jsmPort.toString()))]] }

        }



        //EngineResponseContent response = dockerClient.run(containerCreateRequest, jsmContainerName)
        EngineResponseContent response = dockerClient.createContainer(containerCreateRequest, jsmContainerName)
        assert response.content.warnings.isEmpty(): "Error when creating $jsmContainerName container:" + response.content.warnings.join(",")

        containerId = response.content.id
        return containerId


    }

}
