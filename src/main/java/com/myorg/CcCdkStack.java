package com.myorg;

import software.amazon.awscdk.Stack;
import software.constructs.Construct;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.DefaultStackSynthesizer;

public class CcCdkStack extends Stack {

    public CcCdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        DefaultStackSynthesizer synthesizer = DefaultStackSynthesizer.Builder.create()
            .fileAssetsBucketName("mateocloudtest")
            .bucketPrefix("")
            .cloudFormationExecutionRole("arn:aws:iam::296812326124:role/LabRole")
            .deployRoleArn("arn:aws:iam::296812326124:role/LabRole")
            .fileAssetPublishingRoleArn("arn:aws:iam::296812326124:role/LabRole")
            .imageAssetPublishingRoleArn("arn:aws:iam::296812326124:role/LabRole")
            .build();

        IVpc vpc = Vpc.fromLookup(this, "ExistingVpc", VpcLookupOptions.builder()
            .vpcId("vpc-0ec2d6216e5a2d120")
            .build());

        IRole instanceRole = Role.fromRoleArn(this, "LabRole", "arn:aws:iam::296812326124:role/LabRole");

        LookupMachineImage ubuntuAmi = LookupMachineImage.Builder.create()
            .name("ubuntu/images/hvm-ssd/ubuntu-focal-20.04-amd64-server-*")
            .owners(java.util.Collections.singletonList("099720109477"))  // Canonical como propietario de Ubuntu
            .build();

        Instance instance = Instance.Builder.create(this, "cdktest")
            .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
            .machineImage(ubuntuAmi)
            .vpc(vpc)
            .role(instanceRole)
            .build();

        // Comandos de User Data para instalar y configurar Apache
        instance.addUserData(
            "sudo apt-get update -y",
            "sudo apt-get install -y apache2 git",
            "sudo systemctl start apache2",
            "sudo systemctl enable apache2",
            "sudo echo 'Listen 8001' >> /etc/apache2/ports.conf",
            "sudo echo 'Listen 8002' >> /etc/apache2/ports.conf",
            "sudo git clone https://github.com/mateormz/websimple.git /var/www/websimple",
            "sudo git clone https://github.com/mateormz/webplantilla.git /var/www/webplantilla",
            "echo '<VirtualHost *:8001>' | sudo tee /etc/apache2/sites-available/websimple.conf",
            "echo '    DocumentRoot /var/www/websimple' | sudo tee -a /etc/apache2/sites-available/websimple.conf",
            "echo '    ErrorLog ${APACHE_LOG_DIR}/error.log' | sudo tee -a /etc/apache2/sites-available/websimple.conf",
            "echo '    CustomLog ${APACHE_LOG_DIR}/access.log combined' | sudo tee -a /etc/apache2/sites-available/websimple.conf",
            "echo '</VirtualHost>' | sudo tee -a /etc/apache2/sites-available/websimple.conf",
            "echo '<VirtualHost *:8002>' | sudo tee /etc/apache2/sites-available/webplantilla.conf",
            "echo '    DocumentRoot /var/www/webplantilla' | sudo tee -a /etc/apache2/sites-available/webplantilla.conf",
            "echo '    ErrorLog ${APACHE_LOG_DIR}/error.log' | sudo tee -a /etc/apache2/sites-available/webplantilla.conf",
            "echo '    CustomLog ${APACHE_LOG_DIR}/access.log combined' | sudo tee -a /etc/apache2/sites-available/webplantilla.conf",
            "echo '</VirtualHost>' | sudo tee -a /etc/apache2/sites-available/webplantilla.conf",
            "sudo a2ensite websimple",
            "sudo a2ensite webplantilla",
            "sudo systemctl restart apache2"
        );

        // Permisos de red para los puertos 8001, 8002, 80 y 22
        instance.getConnections().allowFromAnyIpv4(Port.tcp(80), "Allow HTTP traffic on port 80");
        instance.getConnections().allowFromAnyIpv4(Port.tcp(22), "Allow SSH traffic on port 22");
        instance.getConnections().allowFromAnyIpv4(Port.tcp(8001), "Allow HTTP traffic on port 8001");
        instance.getConnections().allowFromAnyIpv4(Port.tcp(8002), "Allow HTTP traffic on port 8002");
    }
}