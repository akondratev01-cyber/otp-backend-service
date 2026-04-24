package ru.akondratev.otp.app;

public final class StartupPrinter {

    private StartupPrinter() {
    }

    public static void printBanner() {
        System.out.println();
        System.out.println("   ____  ______ ___    ____             __                __");
        System.out.println("  / __ \\/_  __/ __ \\  / __ )____ ______/ /_____  ____  __/ /");
        System.out.println(" / / / / / / / /_/ / / __  / __ `/ ___/ //_/ _ \\/ __ \\/ / / ");
        System.out.println("/ /_/ / / / / ____/ / /_/ / /_/ / /__/ ,< /  __/ / / / / /  ");
        System.out.println("\\____/ /_/ /_/     /_____/\\__,_/\\___/_/|_|\\___/_/ /_/_/_/   ");
        System.out.println();
        System.out.println("============================================================");
        System.out.println("                     OTP Backend Service                    ");
        System.out.println("============================================================");
    }

    public static void printStep(int step, int totalSteps, String message) {
        System.out.printf("[%d/%d] %s%n", step, totalSteps, message);
    }

    public static void printSummary(int port) {
        System.out.println();
        System.out.println("------------------------------------------------------------");
        System.out.println(" Server URL : http://localhost:" + port);
        System.out.println(" Scheduler  : enabled");
        System.out.println(" Channels   : FILE, EMAIL, TELEGRAM, SMS");
        System.out.println("------------------------------------------------------------");
        System.out.println(" Endpoints:");
        System.out.println("   POST   /auth/register");
        System.out.println("   POST   /auth/login");
        System.out.println("   POST   /auth/logout");
        System.out.println("   GET    /users/me");
        System.out.println("   GET    /admin/users");
        System.out.println("   DELETE /admin/users/{id}");
        System.out.println("   POST   /otp/generate");
        System.out.println("   POST   /otp/validate");
        System.out.println("   GET    /admin/otp-config");
        System.out.println("   PUT    /admin/otp-config");
        System.out.println("------------------------------------------------------------");
        System.out.println(" Status    : server started successfully");
        System.out.println("============================================================");
        System.out.println();
    }
}