/**
 * Jenkins job definitions
 */

freeStyleJob("${SEED_PROJECT}-${SEED_BRANCH}-build") {
    description "Continuous Integration for the Ontrack Jenkins plug-in"
    logRotator(numToKeep = 40)
    deliveryPipelineConfiguration('Commit', 'CI')
    jdk 'JDK7'
    label 'docker'
    scm {
        git {
            remote {
                url PROJECT_SCM_URL
                branch "origin/${BRANCH}"
                credentials PROJECT_SCM_CREDENTIALS
            }
            extensions {
                wipeOutWorkspace()
                localBranch "${BRANCH}"
            }
        }
    }
    triggers {
        scm 'H/5 * * * *'
    }
    steps {
        maven {
            goals 'clean verify'
            mavenInstallation 'Maven-3.2.x'
        }
    }
    publishers {
        archiveJunit("**/target/surefire-reports/*.xml")
        tasks(
                '**/*.java,**/*.groovy,**/*.xml,**/*.html,**/*.js',
                '**/target/**,seed/**',
                'FIXME', 'TODO', '@Deprecated', true
        )
        archiveArtifacts {
            pattern 'target/*.hpi'
        }
    }
}

freeStyleJob("${SEED_PROJECT}-${SEED_BRANCH}-release") {
    description "Release job for the Ontrack Jenkins plug-in"
    logRotator(numToKeep = 40)
    deliveryPipelineConfiguration('Release', 'Publication')
    parameters {
        stringParam('VERSION', '', '')
        stringParam('NEXT_VERSION', '', '')
    }
    jdk 'JDK7'
    label 'docker'
    scm {
        git {
            remote {
                url PROJECT_SCM_URL
                branch "origin/${BRANCH}"
                credentials PROJECT_SCM_CREDENTIALS
            }
            extensions {
                wipeOutWorkspace()
                localBranch "${BRANCH}"
            }
        }
    }
    wrappers {
        toolenv('Maven-3.2.x')
    }
    steps {
        shell """\
export PATH=\${MAVEN_3_2_X_HOME}/bin:\$PATH
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=\${VERSION}

git config --local user.email "jenkins@nemerosa.net"
git config --local user.name "Jenkins"
git commit -am "Release \${VERSION}"
git tag "\${VERSION}"
"""
        maven {
            mavenInstallation('Maven-3.2.x')
            goals('clean deploy')
            providedSettings('JenkinsIOSettings')
        }
        shell """\
export PATH=\${MAVEN_3_2_X_HOME}/bin:\$PATH
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=\${NEXT_VERSION}-SNAPSHOT
git commit -am "Starting \${NEXT_VERSION}"
"""
    }
    publishers {
        git {
            pushOnlyIfSuccess()
            branch('origin', BRANCH)
            tag('origin', '${VERSION}') {
                message('Release ${VERSION}')
                create()
                update()
            }
        }
        tasks(
                '**/*.java,**/*.groovy,**/*.xml,**/*.html,**/*.js',
                '**/target/**,seed/**',
                'FIXME', 'TODO', '@Deprecated', true
        )
        buildDescription('', 'v${VERSION}', '', 'v${VERSION}')
    }
}
