job("ui test") {
    startOn {
        gitPush {
            anyBranchMatching {
                +"main"
                +"markdown"
            }
        }
    }
    container(image = "gradle:9.5.1-jdk21"){
        env["BOT_TG_TOKEN"] = "{{ project:BOT_TG_TOKEN }}"
        shellScript {
            interpreter = "/bin/bash"
            content = """
                    set +e
                    ./gradlew :shared:cleanJvmTest :shared:jvmTest --tests "shared_tests.ui.UI1Test" --console=plain > test.log 2>&1
                    STATUS=${'$'}?
                    set -e
                    
                    if [ ${'$'}STATUS -eq 0 ]; then
                        RESULT="✅ PASSED"
                    else
                        RESULT="❌ FAILED"
                    fi
                    
                    curl -F document=@test.log \
                         -F chat_id=253870633 \
                         -F caption="UI1Test build {{ run:number }} — ${'$'}RESULT" \
                         https://api.telegram.org/bot${'$'}BOT_TG_TOKEN/sendDocument
                    
                    exit ${'$'}STATUS
                """
        }
    }
}

job("publish wasm") {
    startOn {
        gitPush {
            anyBranchMatching {
                +"main"
            }
        }
    }
    container(image = "gradle:9.5.1-jdk21"){
        env["SSH_HOST"] = "{{ project:SSH_HOST }}"
        env["SSH_PRIVATE_KEY"] = "{{ project:SSH_PRIVATE_KEY }}"
        env["BOT_TG_TOKEN"] = "{{ project:BOT_TG_TOKEN }}"
        shellScript {
            interpreter = "/bin/bash"
            content = """
                    apt-get update
                    apt-get install -y rsync openssh-client python3 python3-pip
                    
                    mkdir -p ~/.ssh
                    chmod 700 ~/.ssh
                    
                    echo "${'$'}SSH_PRIVATE_KEY" > ~/.ssh/id_ed25519
                    chmod 600 ~/.ssh/id_ed25519
        
                    eval $(ssh-agent -s)
                    ssh-add ~/.ssh/id_ed25519
                    
                    echo "Host ${'$'}SSH_HOST" >> ~/.ssh/config
                    echo "    StrictHostKeyChecking no" >> ~/.ssh/config
                    echo "    UserKnownHostsFile=/dev/null" >> ~/.ssh/config
                    
                    ./gradlew :shared:wasmJsBrowserDistribution
                   
                    ssh root@${'$'}SSH_HOST "mkdir -p /var/www/cedarjamdemo.3moly.com" 
                    rsync -avz --delete shared/build/dist/wasmJs/productionExecutable/ root@${'$'}SSH_HOST:/var/www/cedarjamdemo.3moly.com/
                    
                    curl -F chat_id=253870633 -F text="compose-data-viz build {{ run:number }}" https://api.telegram.org/bot${'$'}BOT_TG_TOKEN/sendMessage
                """
        }
    }
}

job("build linux arm64") {

    startOn {
        gitPush {
            anyBranchMatching {
                +"linuxarm64"
            }
        }
    }
    host {

        env["SYNC_SERVER_URL"] = "{{ project:CEDAR_SYNC_SERVER_URL }}"
        env["SYNC_SERVER_TOKEN"] = "{{ project:CEDAR_SYNC_SERVER_TOKEN }}"
        env["IS_RELEASE"] = "{{ project:CEDAR_IS_RELEASE }}"

        shellScript {
            interpreter = "/bin/bash"
            content = """
                set -e

                # Enable ARM64 emulation natively on the Space runner VM
                docker run --rm --privileged multiarch/qemu-user-static --reset -p yes

                echo "1. Creating ARM64 container..."
                docker create --cidfile container_id.txt \
                    --platform linux/arm64 \
                    -e SYNC_SERVER_URL \
                    -e SYNC_SERVER_TOKEN \
                    -e IS_RELEASE \
                    -w /workspace \
                    eclipse-temurin:21 \
                    bash -c "apt update && apt install -y dpkg-dev fakeroot rpm libfuse2 libglib2.0-0 && chmod +x gradlew && ./gradlew :shared:packageReleaseDistributionForCurrentOS"

                read CONTAINER_ID < container_id.txt

                echo "2. Copying source code to container: ${'$'}{'$'}CONTAINER_ID"
                docker cp . "${'$'}{'$'}CONTAINER_ID":/workspace

                echo "3. Running the build inside the container..."
                docker start -a "${'$'}{'$'}CONTAINER_ID"

                echo "4. Extracting the build artifacts..."
                mkdir -p shared/build/compose/binaries/
                docker cp "${'$'}{'$'}CONTAINER_ID":/workspace/shared/build/compose/binaries/. shared/build/compose/binaries/
                
                # Cleanup
                docker rm "${'$'}{'$'}CONTAINER_ID"
                rm container_id.txt
            """
        }
        fileArtifacts {
            localPath = "shared/build/compose/binaries/"
            archive = true
            remotePath = "cedarjam/build.zip"
            onStatus = OnStatus.SUCCESS
        }
    }
}