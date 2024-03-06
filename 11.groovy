def isFailure = 'false'
def version
def buildProject(String gitProjPath, String projectName, String branchVersion, String type){
    gradleBin = "/webserver/softwares/gradle/gradle-5.4.1/bin/gradle"
    gitBaseUrl = "https://github.com"
 
try{
    ws("/webserver/jenkins-workspace/ivs-components/ivs-dev/acs-common-dev/${projectName}"){
         stage("clean work space of ${projectName} "){
            sh"rm -rf *"
        }
        stage("dev ${projectName} checkout "){
            git credentialsId: 'ivsgit',
            url: "${gitBaseUrl}/${gitProjPath}/${projectName}.git",
            branch: "${projectName}-${branchVersion}"
            def tagName = sh(returnStdout: true, script: "${gradleBin} --console=plain printversion| grep -Po 'appVersion\": \"\\K([^\"]*)'").trim()          
            def temp = tagName
            echo "${temp}"
            String[] str;
            //str = temp.split('-');
            //str = temp
            version = temp
            echo "${version}"
        }
        stage("dev Build the ${projectName} jar"){
            //sh"${gradleBin} promote"
           //  sh"${gradleBin} bumpversion"
            if(type?.equalsIgnoreCase("lib1")){
                sh"${gradleBin} clean publish"
            }else{
                sh"${gradleBin} clean appdistzip"
            }
        }
    }
}

    catch (Exception e) {
        isFailure = true;
     stage('sending failure email notification'){
         emailext (
            from: 'ivs.build@wibmo.com',           
            to: 'pathi.rangasaikumar@wibmo.com, suresh.madisetty@wibmo.com',
            subject: "Testing ivs-dev-common build failed",
            body: 'Testing body failed'
         )
        }
    }

  }
def buildLibonly(String gitProjPath, String projectName, String branchVersion){
    buildProject(gitProjPath, projectName, branchVersion, "lib1")
}

def startNextJob(){
    echo "Build Success start next job"
    echo "${version}"
    build job: '12_acs_core_dev_withcommon', parameters: [[$class: 'StringParameterValue', name: 'common', value: version ]]
}

def sendSuccessMail(){
    stage('sending success email notification'){
         emailext (
            from: 'ivs.build@wibmo.com',           
            to: 'pathi.rangasaikumar@wibmo.com, suresh.madisetty@wibmo.com',
            subject: "Testing subject success",
            body: 'Testing body success'
         )
        }
    }

node { 
  
    // Libraries
  // common
    buildLibonly('wibmo-payment-security','wibmo-json-utility',"${params.branch}")
    /*buildLibonly('wibmo-payment-security','3ds-entity',"${params.branch}")
    buildLibonly('wibmo-payment-security','acs2-entity',"${params.branch}")
    buildLibonly('wibmo-payment-security','accosa2-cache-client',"${params.branch}")
    //buildLibonly('wibmo-payment-security','3ds-server-entity',"${params.branch}")
    buildLibonly('wibmo-payment-security','accosa2-rediscache-cluster',"${params.branch}")
    buildLibonly('wibmo-payment-security','accosa2-sync-httpclient',"${params.branch}")
    buildLibonly('wibmo-payment-security','accosa2-async-httpclient',"${params.branch}")
    buildLibonly('wibmo-payment-security','accosa2-logging',"${params.branch}")
    buildLibonly('wibmo-payment-security','accosa2-hsm-restclient',"${params.branch}")
   
    buildLibonly('wibmo-payment-security','encrypt-utils-lib',"${params.branch}")
    buildLibonly('wibmo-payment-security','acs2-cache',"${params.branch}")
    buildLibonly('wibmo-payment-security','rba-validator',"${params.branch}")
    buildLibonly('wibmo-payment-security','acs2-validator',"${params.branch}")
    buildLibonly('wibmo-payment-security','accosa2-config-management-client',"${params.branch}")
    buildLibonly('wibmo-payment-security','accosa2-vertx-bootstrap',"${params.branch}")
    //buildLibonly('wibmo-payment-security','3ds-server-cache',"${params.branch}")
    */
   echo isFailure
    sh'sleep 5'
    if (isFailure == "false"){
        sendSuccessMail()
        startNextJob()
    }

 }
