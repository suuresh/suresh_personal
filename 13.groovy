def version
def buildProject(String gitProjPath, String projectName, String branchVersion, String type){
    gradleBin = "/webserver/softwares/gradle/gradle-5.4.1/bin/gradle"
    gitBaseUrl = "https://github.com"    

    ws("/webserver/jenkins-workspace/ivs-components/ivs-dev/acs-core-dev-withoutcommon/${projectName}"){
        git credentialsId: 'ivsgit',
        url: "${gitBaseUrl}/${gitProjPath}/${projectName}.git",
        branch: "${projectName}-${branchVersion}"
        def tagName = sh(returnStdout: true, script: "${gradleBin} --console=plain printversion| grep -Po 'appVersion\": \"\\K([^\"]*)'").trim()          
        def temp = tagName
        String[] str;
        str = temp.split('-');
        version = str[0]    

        stage("${projectName} checkout"){
            git credentialsId: 'ivsgit',
            url: "${gitBaseUrl}/${gitProjPath}/${projectName}.git",
            branch: "${projectName}-${branchVersion}"
        }

        stage("Build the ${projectName} jar"){
            //            sh "${gradleBin} clean"
            //       def tagName = sh(returnStdout: true, script: "${gradleBin} --console=plain printversion| grep -Po 'appVersion\": \"\\K([^\"]*)'").trim()          
            // sh "echo here $tagName"
    
            if(type?.equalsIgnoreCase("lib1")){
                sh"${gradleBin} clean publish"
            }
            /*else{
                sh"${gradleBin} clean appdistzip"
            }*/
        }
        stage("Build the ${projectName} jar"){
            sh "${gradleBin} clean "
            if(type?.equalsIgnoreCase("service1")){
                sh"${gradleBin} clean publish"
            }
            stage("Build the ${projectName} jar"){
                sh "${gradleBin} clean "
                if(type?.equalsIgnoreCase("service2")){
                    sh "rm -rf /webserver/softwares/apache-tomcat/apache-tomcat-10.1.5/webapps/test/${projectName}/build/reports"
                    sh "rm -rf reports"
                    
                    sh"${gradleBin} clean build"
                    //sh"${gradleBin} clean appdistzip"
                    sh"${gradleBin} appdistzip"
                }                
            }
            stage("moving ${projectName} jar into common location"){
                if(type?.equalsIgnoreCase("service2")){
                    sh "cp build/distributions/*.zip /webserver/jenkins-workspace/ivs-components/ivs-dev/acs-core-final/"
                    sh "cp -r build/reports/ /webserver/softwares/apache-tomcat/apache-tomcat-10.1.5/webapps/test/${projectName}/build/"
                }
            }
        }
    }
    
}

def changelog(){
    stage('sending email notification'){
        def changelines='''$CHANGES'''
        def cleanedChangeLines = ""
        changelines.eachLine {
        if (it.contains('Tagging build version') || ('Update version.json')) {
                //Skip the values
            } else {
            if (it?.trim()?.length() > 0)
                cleanedChangeLines += "$it\n\n"
            }
        }
        // emailext body: '''<div style="padding-left: 30px; padding-bottom: 15px;">
        //${CHANGES, showPaths=true, format="<div><b>%a</b>: %r %p </div><div style=\"padding-left:30px;\"> &#8212; &#8220;<em>%m</em>&#8221;</div>", pathFormat="</div><div style=\"padding-left:30px;\">%p"}
        //</div>''', 
        emailext body: "<pre> $cleanedChangeLines <pre>",
        subject: ' Acs-$BUILD_NUMBER Change log', to: 'gangala.vijayakumar@wibmo.com '
    }
}


def services(String gitProjPath, String projectName, String branchVersion){
    buildProject(gitProjPath, projectName, branchVersion, "service2")
}
def libservices(String gitProjPath, String projectName, String branchVersion){
    buildProject(gitProjPath, projectName, branchVersion, "service1")    
}
/*
def publishProject(String gitProjPath, String projectName, String branchVersion, String type){
    gradleBin = "/usr/bin/gradle"
    gitBaseUrl = "https://git.enstage-sas.com"
    ws("/appserver/jenkins-workspace/acs2/qa/${projectName}"){
        stage("${projectName} checkout"){
            git credentialsId: '1212',
            url: "${gitBaseUrl}/${gitProjPath}/${projectName}.git",
            branch: "${projectName}-${branchVersion}"
        }
        stage("Build the ${projectName} jar"){
            sh"${gradleBin} promote"
       //     def tagName = sh(returnStdout: true, script: "gradle --console=plain printversion|grep -Po \'appVersion: \'").trim()

            def tagName = sh(returnStdout: true, script: "gradle --console=plain printversion|grep -Po \'${projectName}:\\K(.*)\'").trim()
            sh "echo here $tagName"
            sh "git tag $tagName"
            sh "git push origin --tags"
            if(type?.equalsIgnoreCase("lib")){
                sh"${gradleBin} publish"
            }else{
                sh"${gradleBin} appdistzip"
            }
            sh"${gradleBin} bumpversion"
            sh "git init && git add version.txt && git commit -m 'Tagging build version ${tagName}' && git push origin '${projectName}-${branchVersion}' "
        }
        stage("moving ${projectName} jar into common location"){
            if(type?.equalsIgnoreCase("service")){
            	sh "cp build/distributions/*.zip /appserver/jenkins-workspace/zip_components/acs2_zip/final_versions/"
            }
        } 
        
        
    }
}
*/
def buildLib(String gitProjPath, String projectName, String branchVersion){
    publishProject(gitProjPath, projectName, branchVersion, "lib")
}

def buildService(String gitProjPath, String projectName, String branchVersion){
    publishProject(gitProjPath, projectName, branchVersion, "service")
}

def startNextJob(){
    echo "Build Success start next job"
    echo "${version}"
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
        currentBuild.displayName = 'build from '+"${params.branch}"
        service = "acs2-service-clients"
        libservices('wibmo-payment-security','acs2-service-clients',"${params.branch}")
        service = "acs2-lib"
        libservices('wibmo-payment-security','acs2-lib',"${params.branch}")
        service = "customer-api-impl"
        libservices('wibmo-payment-security','customer-api-impl',"${params.branch}")
        service = "oob-service-impl"
        libservices('wibmo-payment-security','oob-service-impl',"${params.branch}")
        service = "alert-service-impl"
        libservices('wibmo-payment-security','alert-service-impl',"${params.branch}")



        //service

        //services('wibmo-payment-security','accosa2-control-plane',"${params.branch}")
            
        //service
        service = "acs2-job-scheduler"
        services('wibmo-payment-security','acs2-job-scheduler',"${params.branch}")
        service = "acs2-issuer-config-api"
        services('wibmo-payment-security','acs2-issuer-config-api',"${params.branch}")
        service = "acs2-alert-management"
        services('wibmo-payment-security','acs2-alert-management',"${params.branch}")
        service = "acs2-expiry-service"
        services('wibmo-payment-security','acs2-expiry-service',"${params.branch}")
        service = "oob-service"
        services('wibmo-payment-security','oob-service',"${params.branch}")
        service = "accosa2-config-management"
        services('wibmo-payment-security','accosa2-config-management',"${params.branch}")
        service = "acs2-api"
        services('wibmo-payment-security','acs2-api',"${params.branch}")
        service = "otp-engine"    
        services('wibmo-payment-security','otp-engine',"${params.branch}")
        service = "acs2-web-registration"
        services('wibmo-payment-security','acs2-web-registration',"${params.branch}")
        service = "xdc-data-sync"
        services('wibmo-payment-security','xdc-data-sync',"${params.branch}")
        service = "acs2-datastore-api"
        services('wibmo-payment-security','acs2-datastore-api',"${params.branch}")

        /*
        services('wibmo-payment-security','acs2-customer-data-api',"${params.branch}")    
        services('wibmo-payment-security','acs2-ui',"${params.branch}")
        services('wibmo-payment-security','acs-legacy-api',"${params.branch}")
        services('wibmo-payment-security','acs-legacy-ui',"${params.branch}")
        services('wibmo-payment-security','acs2-rupay',"${params.branch}")    
        services('wibmo-payment-security','acs2-enroll',"${params.branch}")
        //services('wibmo-payment-security','accosa2-payout-hub',"${params.branch}")
        //services('wibmo-payment-security','accosa2-payout-spoke',"${params.branch}")     
        
        
        //changelog()
        //  zip('accosa2/extras/network-management','accosa2-config-management',"${params.branch}")
        */
        sendSuccessMail()
        startNextJob()
 
    }
    catch (Exception e) {

        echo service + " failed"    
        stage('sending failure email notification'){
         emailext (
            from: 'ivs.build@wibmo.com',           
            to: 'pathi.rangasaikumar@wibmo.com, suresh.madisetty@wibmo.com',
            subject: service + "is failed to build, Please check",
            body: service + "is failed to build, Please check"
         )
        }
    }

}
  