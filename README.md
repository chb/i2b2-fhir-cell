#Description#

I2B2 FHIR CELL offers a FHIR 1.0.2 REST API compliant to push data into I2B2.

###Other external systems###

* I2B2: The biomedical integration software system. https://www.i2b2.org/

#I2B2 FHIR Cell Installation#  

###Dependencies###

java7, maven 3, JBoss AS7 

###I2B2 FHIR CELL configuration###

The cell uses the FR and CRC i2b2 cells to store data. Thus, an instance of i2b2 should be accessible via http. Configuration details are set in *config.properties* files for each environment (dev, qa, prod)

#Building the war from source
Clone the project in a local directory, access it and:

    mvn clean package

By default the war is configured for development environment. For QA and production environment respectively:

    mvn clean package -Dqa
    mvn clean package -Dprod

It will execute automatically all unit tests. To execute unit tests without compiling:

    mvn test
     
To execute integration tests using Arquillian on an existing JBoss AS server instance running:

    mvn clean -Dtest=*IT test -Parq-jbossas-remote
    
To execute integration tests using Arquillian with no JBoss server running (it will start one and will do all the necessary deployments):

    mvn clean -Dtest=*IT test -Parq-jbossas-managed


#Deploying the war

If JBoss is installed in the same machine:

    mvn jboss-as:deploy
    
If JBoss is somewhere else, just copy the generated war to 

    $JBOSS_HOME/standalone/deployments
    
Also, it is possible to use the administration web application of JBoss to deploy war files remotely, by accessing the root of the port 9990 in a navigator. In a local installation:

    http://localhost:9990/
    
It requires an administration user of JBoss, which can be added in the JBoss server as follows:

    cd $JBOSS_HOME/bin
    ./add-user.sh
    
#Testing the correct installation

If deployed in a local workbench with default configuration parameters the following URL should respond a welcome message:

    http://127.0.0.1:8080/fhir-i2b2
    
There is also a getEcho Rest method that can be tested in the browser as follows:

    http://127.0.0.1:8080/fhir-i2b2/rest/echo/getEcho/testing_the_echo
    
It should produce the following JSON String

     {"var":"Echo: testing_the_echo"}
     
#Deploying in an i2b2 server

The easiest way is to create a JBoss Admin user in i2b2 server and use the Admin web application at port 9990 to upload the generated war file.    

# Setting Development Environment with VAGRANT (using VirtualBox ubuntu/trusty64)

Grap the box trusty64:

    vagrant box add trusty64 file://{path to trusty64.box}

or

    vagrant box add ubuntu/trusty64

Once source code is cloned, in the root of the project:

    vagrant up

It it's the first execution, it will provision automatically by executing the file 'bootstrap.sh' in the VM.

To connect to the VM

    vagrant ssh

In the VM, the directory /vagrant is synchronized with the root host source folder.
The ports mapping redirects guess port 8080 to 8888. It can be changed in the vagrant configuration file.
So, from the host it is possible to access the VM as

    http://127.0.0.1:8888
