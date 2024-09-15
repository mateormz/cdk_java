package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class CcCdkApp {
        public static void main(final String[] args) {
            App app = new App();
    
            // Crear el entorno para el stack
            Environment env = Environment.builder()
                .account("296812326124")  // Tu Account ID
                .region(System.getenv("CDK_DEFAULT_REGION"))  // Región de AWS
                .build();
    
            // Crear una instancia de CcCdkStack pasando el entorno en las propiedades
            new CcCdkStack(app, "CcCdkStack", StackProps.builder()
                .env(env)
                .build());
    
            app.synth();
        }
    }