package org.example;


import org.example.model.WalletSystem;
import org.example.service.ApplicationService;
import org.example.service.impl.ApplicationServiceimpl;

public class Main
{
    public static void main(String[] args)
    {

        ApplicationService applicationService=new ApplicationServiceimpl();
        applicationService.start();

    }
}
