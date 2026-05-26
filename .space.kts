job("coverage badge") {
    startOn {
        gitPush {
            anyBranchMatching {
                +"main"
                +"work_markdown"
            }
        }
    }
    container(image = "3moly/cedar-ui-test:latest") {
        env["BOT_TG_TOKEN"]      = "{{ project:BOT_TG_TOKEN }}"
        env["SYNC_SERVER_URL"]   = "{{ project:CEDAR_SYNC_SERVER_URL }}"
        env["SYNC_SERVER_TOKEN"] = "{{ project:CEDAR_SYNC_SERVER_TOKEN }}"
        env["IS_RELEASE"]        = "{{ project:CEDAR_IS_RELEASE }}"
        env["GIST_SECRET"]       = "{{ project:GIST_SECRET }}"
        env["GIST_ID"]           = "{{ project:GIST_ID }}"
        env["CI"] = "true"

        shellScript {
            interpreter = "/bin/bash"
            content = """
                set -e

                #  Run tests + Kover XML report (xvfb for any UI-touching tests)
                xvfb-run -a --server-args="-screen 0 1280x1024x24" \
                    ./gradlew :shared:cleanJvmTest :shared:jvmTest --tests "shared_tests.ui.MarkdownTest" :shared:koverXmlReportCustom \
                    --build-cache --no-daemon --console=plain

                # Parse coverage XML
                read PERCENT COLOR < <(python3 -c "
                import xml.etree.ElementTree as ET
                root = ET.parse('shared/build/reports/kover/customJvm/report.xml').getroot()
                line = next(c for c in root.findall('counter') if c.get('type') == 'LINE')
                covered = int(line.get('covered'))
                missed  = int(line.get('missed'))
                pct = (covered / (covered + missed)) * 100
                color = 'brightgreen' if pct >= 80 else 'yellow' if pct >= 60 else 'red'
                print(f'{pct:.2f} {color}')
                ")

                echo "Coverage: ${'$'}PERCENT% (${'$'}COLOR)"

                # Build shields.io endpoint JSON
                cat > kover-coverage.json <<EOF
                {
                  "schemaVersion": 1,
                  "label": "Coverage",
                  "message": "${'$'}PERCENT%",
                  "color": "${'$'}COLOR"
                }
                EOF

                # PATCH the gist directly via GitHub API
                # GIST_SECRET must be a GitHub PAT with `gist` scope
                PAYLOAD=${'$'}(python3 -c "
                import json, sys
                with open('kover-coverage.json') as f:
                    content = f.read()
                print(json.dumps({'files': {'kover-coverage.json': {'content': content}}}))
                ")

                curl -sS -X PATCH \
                    -H "Authorization: token ${'$'}GIST_SECRET" \
                    -H "Accept: application/vnd.github+json" \
                    -d "${'$'}PAYLOAD" \
                    "https://api.github.com/gists/${'$'}GIST_ID"

                # Telegram notification
                curl -F chat_id=253870633 \
                     -F text="Coverage badge updated: ${'$'}PERCENT% (build {{ run:number }})" \
                     https://api.telegram.org/bot${'$'}BOT_TG_TOKEN/sendMessage
            """
        }
    }
}
job("ui test") {
    startOn {
        gitPush {
            anyBranchMatching {
                +"main"
                +"markdown"
            }
        }
    }
    container(image = "3moly/cedar-ui-test:latest") {
        env["BOT_TG_TOKEN"] = "{{ project:BOT_TG_TOKEN }}"
        env["SYNC_SERVER_URL"]   = "{{ project:CEDAR_SYNC_SERVER_URL }}"
        env["SYNC_SERVER_TOKEN"] = "{{ project:CEDAR_SYNC_SERVER_TOKEN }}"
        env["IS_RELEASE"]        = "{{ project:CEDAR_IS_RELEASE }}"
        env["CI"] = "true"

        shellScript {
            interpreter = "/bin/bash"
            content = """
                    # Dependencies are pre-installed in the custom image
                    
                    set +e
                    # Added --build-cache and --no-daemon for speed and reliability
                    xvfb-run -a --server-args="-screen 0 1280x1024x24" \
                        ./gradlew :shared:cleanJvmTest :shared:jvmTest --tests "shared_tests.*" :shared:koverHtmlReportJvm \
                        --build-cache --no-daemon --console=plain > test.log 2>&1
                    
                    STATUS=${'$'}?
                    set -e
                    
                    if [ ${'$'}STATUS -eq 0 ]; then
                        RESULT="✅ PASSED"
                    else
                        RESULT="❌ FAILED"
                    fi
                    
                    # Reporting remains the same
                    curl -F document=@test.log \
                         -F chat_id=253870633 \
                         -F caption="UI1Test build {{ run:number }} — ${'$'}RESULT" \
                         https://api.telegram.org/bot${'$'}BOT_TG_TOKEN/sendDocument
                    
                    if [ -d shared/build/reports/tests/jvmTest ]; then
                        (cd shared/build/reports/tests/jvmTest && zip -r /tmp/report.zip .)
                        curl -F document=@/tmp/report.zip \
                             -F chat_id=253870633 \
                             -F caption="UI1Test report {{ run:number }}" \
                             https://api.telegram.org/bot${'$'}BOT_TG_TOKEN/sendDocument
                    fi
                    
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