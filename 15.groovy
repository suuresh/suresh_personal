def deploy(String cluster_name, String projectName, String release, String version,String appserver, String acs2, String log ){
    ws('/webserver/automation/ansible/ansible-scripts/acs2_deploy'){
        stage("Running ${projectName}"){
            ansiColor('xterm') {
                ansiblePlaybook( 
                    installation: 'ansible',
                    playbook: 'accosa2_dev.yml',
                    inventory: '/webserver/automation/ansible/inventory/acs2/${cluster_name}', 
                    extras: "-ve ' component=acs2 baseDir=appserver logBaseDir=log clusterName=${cluster_name} project=${projectName} version=${version} release=${release}'",
                    colorized: true
                ) 
            }       
        }
    }
}

node{
    //currentBuild.displayName = 'ON'+"${params.cluster_name}"+currentBuild.number
    currentBuild.displayName = "${params.version}"+' ON '+"${params.cluster_name}"
    deploy("${params.cluster_name}",'accosa2-config-management',"${params.release}","${params.version}",'appserver','acs2','log')
    deploy("${params.cluster_name}",'accosa2-control-plane',"${params.release}","${params.version}",'appserver','acs2','log')
    deploy("${params.cluster_name}",'acs2-issuer-config-api',"${params.release}","${params.version}",'appserver','acs2','log')
    deploy("${params.cluster_name}",'acs2-datastore-api',"${params.release}","${params.version}",'appserver','acs2','log')
    deploy("${params.cluster_name}",'otp-engine',"${params.release}","${params.version}",'appserver','acs2','log')
    deploy("${params.cluster_name}",'acs2-alert-management',"${params.release}","${params.version}",'appserver','acs2','log')
    deploy("${params.cluster_name}",'acs2-customer-data-api',"${params.release}","${params.version}",'appserver','acs2','log')
    deploy("${params.cluster_name}",'acs2-api',"${params.release}","${params.version}",'appserver','acs2','log')
    deploy("${params.cluster_name}",'acs2-ui',"${params.release}","${params.version}",'appserver','acs2','log')
    deploy("${params.cluster_name}",'acs-legacy-api',"${params.release}","${params.version}",'appserver','acs2','log')
    deploy("${params.cluster_name}",'acs-legacy-ui',"${params.release}","${params.version}",'appserver','acs2','log')
    deploy("${params.cluster_name}",'xdc-data-sync',"${params.release}","${params.version}",'appserver','acs2','log')
    deploy("${params.cluster_name}",'oob-service',"${params.release}","${params.version}",'appserver','acs2','log')
    
    deploy("${params.cluster_name}",'acs2-expiry-service',"${params.release}","${params.version}",'appserver','acs2','log')
    deploy("${params.cluster_name}",'acs2-job-scheduler',"${params.release}","${params.version}",'appserver','acs2','log')


}

