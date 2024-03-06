def isFailure = "false"
def version
def buildProject(String gitProjPath, String projectName, String branchVersion, String common, String type){
    gradleBin = "/webserver/softwares/gradle/gradle-5.4.1/bin/gradle"
    gitBaseUrl = "https://github.com"   

    ws("/webserver/jenkins-workspace/ivs-components/ivs-dev/acs-core-dev/${projectName}"){
        stage("test ${projectName} checkout "){
            git credentialsId: 'ivsgit',
            url: "${gitBaseUrl}/${gitProjPath}/${projectName}.git",
            branch: "${projectName}-${branchVersion}"
        }
        stage(" Building the ${projectName} jar"){
            // sh"${gradleBin} promote"
            sh"${gradleBin} updateVersion -Pversion.commonVersion=${common}"
            if(type?.equalsIgnoreCase("lib")){
                sh"${gradleBin} clean publish"
            }else{
                sh "rm -rf /webserver/softwares/apache-tomcat/apache-tomcat-10.1.5/webapps/test/${projectName}/build/reports"
                sh "rm -rf reports"
                sh"${gradleBin} clean build"
                sh "cp -r build/reports/ /webserver/softwares/apache-tomcat/apache-tomcat-10.1.5/webapps/test/${projectName}/build/"
                sh"${gradleBin} clean appdistzip"
            }
            // def tagName = sh(returnStdout: true, script: "${gradleBin} --console=plain printversion| grep -Po 'appVersion\": \"\\K([^\"]*)'").trim()          
            // sh "echo here $tagName"
            // sh "git tag $tagName"
            // sh "git push origin --tags"
            // sh"${gradleBin} bumpversion"
            sh "git init && git add version.json && git commit -m 'Tagging build version ' && git push origin '${projectName}-${branchVersion}' "
        }
        
        stage("moving ${projectName} jar into common location"){
            if(type?.equalsIgnoreCase("service")){
                sh "cp build/distributions/*.zip /webserver/jenkins-workspace/ivs-components/ivs-dev/acs-core-final/"
                
            }
        } 
        /*stage("moving ${projectName} jar into common location"){
            if(projectName?.equalsIgnoreCase("accosa2-control-plane")){
                sh "cp build/distributions/*.zip /backup/jenkins-workspace/zip_components/3ds2_zip/final_versions/"
            }
        }
        */
    }    
}

def zip(String gitProjPath, String projectName, String branchVersion){
    ws("/webserver/jenkins-workspace/ivs-components/ivs-dev/acs-core-dev/${projectName}"){
        stage('Creating  tar from all zip files')
        {
        def cvs = sh(returnStdout: true, script: "${gradleBin} --console=plain printversion| grep -Po 'appVersion\": \"\\K([^\"]*)'").trim()
        sh "echo here $cvs"
        // sh "cd /appserver/jenkins-workspace/zip_components/acs2_zip/final_versions/; tar -cvf acs2-'$cvs'.tar *-'$cvs'.zip "
        
        }
        stage('Move  Tar file to  zip location ')
        {
            sh "cd /backup/jenkins-workspace/zip_components/acs2_zip/final_versions/; mv acs2-'$cvs'.tar .. "
        }
    }
}

def buildLib(String gitProjPath, String projectName, String branchVersion, String common){      
    buildProject(gitProjPath, projectName, branchVersion, common, "lib")
}

def buildService(String gitProjPath, String projectName, String branchVersion ,String common){
    buildProject(gitProjPath, projectName, branchVersion, common, "service")
}

/*def changelog(){
stage('sending email notification'){
    
       emailext body: '<pre> $CHANGES <pre>', 
       subject: ' Acs build $BUILD_NUMBER Change log', to: 'gangala.vijayakumar@wibmo.com, 3ds2.0qa@wibmo.com, 3ds2.0managers@wibmo.com , 3ds2.0dev@wibmo.com ,pathi.rangasaikumar@wibmo.com'
        }
}
*/
def startNextJob(){
    echo "Build Success start next job"
    //echo "${version}"
    build job: '14_acs_core_dev_zip', parameters: [[$class: 'StringParameterValue', name: 'version', value: version ]]
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

    def service = ""
    try{
        //libs services
        service = "acs2-service-clients"
        buildLib('wibmo-payment-security','acs2-service-clients',"${params.branchVersion}","${params.common}")
        service = "acs2-lib"
        buildLib('wibmo-payment-security','acs2-lib',"${params.branchVersion}","${params.common}")
        service = "customer-api-impl"
        buildLib('wibmo-payment-security','customer-api-impl',"${params.branchVersion}","${params.common}")
        service = "oob-service-impl"
        buildLib('wibmo-payment-security','oob-service-impl',"${params.branchVersion}","${params.common}")
        service = "alert-service-impl"
        buildLib('wibmo-payment-security','alert-service-impl',"${params.branchVersion}","${params.common}")
        
        // services
        service = "accosa2-config-management"
        buildService('wibmo-payment-security','accosa2-config-management',"${params.branchVersion}","${params.common}")
        service = "accosa2-control-plane"
        buildService('wibmo-payment-security','accosa2-control-plane',"${params.branchVersion}","${params.common}")
        service = "acs2-issuer-config-api"
        buildService('wibmo-payment-security','acs2-issuer-config-api',"${params.branchVersion}","${params.common}")
        service = "acs2-datastore-api"
        buildService('wibmo-payment-security','acs2-datastore-api',"${params.branchVersion}","${params.common}")
        service = "otp-engine"
        buildService('wibmo-payment-security','otp-engine',"${params.branchVersion}","${params.common}")
        service = "acs2-alert-management"
        buildService('wibmo-payment-security','acs2-alert-management',"${params.branchVersion}","${params.common}")
        service = "acs2-customer-data-api"
        buildService('wibmo-payment-security','acs2-customer-data-api',"${params.branchVersion}","${params.common}")
        service = "acs2-api"
        buildService('wibmo-payment-security','acs2-api',"${params.branchVersion}","${params.common}")
        service = "acs2-ui"
        buildService('wibmo-payment-security','acs2-ui',"${params.branchVersion}","${params.common}")
        service = "acs-legacy-api"
        buildService('wibmo-payment-security','acs-legacy-api',"${params.branchVersion}","${params.common}")
        service = "acs-legacy-ui"
        buildService('wibmo-payment-security','acs-legacy-ui',"${params.branchVersion}","${params.common}")
        service = "acs2-rupay"
        buildService('wibmo-payment-security','acs2-rupay',"${params.branchVersion}","${params.common}")
        service = "acs2-enroll"
        buildService('wibmo-payment-security','acs2-enroll',"${params.branchVersion}","${params.common}")
        
        //buildService('wibmo-payment-security','accosa2-payout-hub',"${params.branchVersion}","${params.common}")
        //buildService('wibmo-payment-security','accosa2-payout-spoke',"${params.branchVersion}","${params.common}")
        service = "xdc-data-sync"
        buildService('wibmo-payment-security','xdc-data-sync',"${params.branchVersion}","${params.common}")
        service = "oob-service"
        buildService('wibmo-payment-security','oob-service',"${params.branchVersion}","${params.common}")
        service = "acs2-web-registration"
        buildService('wibmo-payment-security','acs2-web-registration',"${params.branchVersion}","${params.common}")
        service = "acs2-expiry-service"
        buildService('wibmo-payment-security','acs2-expiry-service',"${params.branchVersion}","${params.common}")
        service = "acs2-job-scheduler"
        buildService('wibmo-payment-security','acs2-job-scheduler',"${params.branchVersion}","${params.common}")
        
        //changelog()
        //  zip('wibmo-payment-security','accosa2-config-management',"${params.branch}")
        sendSuccessMail()
        startNextJob()
        
    }
    catch (Exception e) {   
        echo service + " is failed"
        stage('sending failure email notification') {
        
        // emailext (
        //     from: 'ivs.build@wibmo.com',           
        //     to: 'pathi.rangasaikumar@wibmo.com, suresh.madisetty@wibmo.com',
        //     subject: "Compilation issue in the acs-core services",
        //     body: service + ' is faild to compile. Please resolve the build issue and also check the unit testing code coverage criteria'
        //  )
        }
    }

}
