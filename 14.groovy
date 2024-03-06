node{

  //ws("/webserver/jenkins-workspace/$JOB_NAME")
  ws("/webserver/jenkins-workspace/ivs-components/ivs-dev/acs-core-zip/acs-core-zip")
 {
/*
stage('Move Creating  tar of zip files')
     {
sh 'cd /appserver/jenkins-workspace/zip_components/acs2_zip/final_versions/; rm -rf *.tar.gz '
  }
  */
stage('Move Creating  tar of zip files')
     {
         echo "${version}"
         
sh "cd /webserver/jenkins-workspace/ivs-components/ivs-dev/acs-core-final/; tar -cvf acs2-'${version}'-SNAPSHOT.tar *'${version}'-SNAPSHOT.zip "
//sh "cd /backup/jenkins-workspace/zip_components/acs2_zip/final_versions; mv acs2-'${version}'-SNAPSHOT.tar .. "
  }

}

    echo "Build Success start next job"
    echo "${version}"
    build job: '15_acs_core_dev_deploy', parameters: [[$class: 'StringParameterValue', name: 'version', value: version ]]


}
