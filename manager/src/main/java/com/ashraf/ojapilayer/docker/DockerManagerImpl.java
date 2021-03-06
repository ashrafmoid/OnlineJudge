package com.ashraf.ojapilayer.docker;

import com.ashraf.ojapilayer.enums.ContainerStatus;
import com.ashraf.ojapilayer.models.BuildImageCreationRequest;
import com.ashraf.ojapilayer.models.Container;
import com.ashraf.ojapilayer.models.ContainerCreationRequest;
import com.ashraf.ojapilayer.models.CreateContainerResponse;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Log4j2
public class DockerManagerImpl implements DockerManager {

    private final DockerClient dockerClient;
    private static final String dockerMountPath = "/usr/local/submission/";

    @Override
    public CreateContainerResponse createContainer(ContainerCreationRequest containerCreationRequest) throws DockerException, InterruptedException {
        final String imageNameWithVersion = containerCreationRequest.getImageName() +
                (Objects.nonNull(containerCreationRequest.getImageVersion()) ?
                        ":" + containerCreationRequest.getImageVersion() : StringUtils.EMPTY);
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        portBindings.put(containerCreationRequest.getContainerPort(), List.of(PortBinding.of("0.0.0.0", containerCreationRequest.getHostPort())));
        final HostConfig.Bind bind = HostConfig.Bind.from(containerCreationRequest.getVolume())
                .to(dockerMountPath)
                .build();
        final ContainerConfig containerConfig = ContainerConfig.builder().image(imageNameWithVersion)
                        .cmd(containerCreationRequest.getCommand()).networkDisabled(true)
                .hostConfig(HostConfig.builder().portBindings(portBindings).appendBinds(bind).build())
                .volumes(containerCreationRequest.getVolume())
                .build();
        final ContainerCreation containerCreation = dockerClient.createContainer(containerConfig);
        return CreateContainerResponse.builder().id(containerCreation.id()).warnings(containerCreation.warnings()).build();
    }

    @Override
    public List<Container> getAllContainersWithStatus(ContainerStatus status) throws DockerException, InterruptedException {
        return dockerClient.listContainers(DockerClient.ListContainersParam.allContainers()).stream()
                .filter(container -> container.state().equals(status.getValue()))
                .map(container -> Container.builder().imageId(container.imageId()).id(container.id())
                        .status(container.state()).image(container.image()).build())
                .collect(Collectors.toList());

    }

    @Override
    public String buildImageFromFile(BuildImageCreationRequest request) throws DockerException, IOException, InterruptedException {
        return dockerClient.build(
                Paths.get(request.getDockerContextPath()), progressMessage -> {},
               DockerClient.BuildParam.name(request.getImageName()));
    }

    @Override
    public void startContainerWithId(String id) throws DockerException, InterruptedException {
        dockerClient.startContainer(id);
    }

    @Override
    public void stopContainerWithId(String id) throws DockerException, InterruptedException {
        dockerClient.stopContainer(id, 1);

    }

    @Override
    public void killContainerWithId(String id) throws DockerException, InterruptedException {
        dockerClient.killContainer(id);
    }

    @Override
    public boolean isContainerRunning(String containerId) throws DockerException, InterruptedException {
       return dockerClient.listContainers(DockerClient.ListContainersParam.withStatusRunning())
                .stream().anyMatch(container -> containerId.equals(container.id()));
    }
}
