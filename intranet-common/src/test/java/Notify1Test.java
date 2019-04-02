import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Notify1Test {

    public static void main(String[] args) throws InterruptedException {
        WaitTest waitTest = new WaitTest();
        waitTest.start();
        for (int x = 1; x < 10; x++) {
            System.out.println("num" + x);
            new Thread(String.valueOf(x)) {
                @Override
                public void run() {
                    waitTest.print(getName());
                }
            }.start();
        }
        waitTest.notify();
        waitTest.notify();
    }


}

class WaitTest extends Thread {
    @Override
    public void run() {
        try {
            waitTest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void waitTest() throws InterruptedException {
        wait();
    }

    public synchronized void print(String name) {
        System.out.println(name);
    }
}

class Temp {
    int count = 0;

    public void waiter() throws InterruptedException {
        synchronized (this) {
            System.out.println("等待" + count++);
            wait();
/*            for (int i = 0; i < 10; i++) {
                System.out.println(Thread.currentThread().getName() +"              "+ this.count);
            }*/
        }
    }

    public void notifyer() throws InterruptedException {
        synchronized (this) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("唤醒");
            for (int i = 0; i < 10; i++) {
                System.out.println(Thread.currentThread() + "notifyer:" + i);
                //count += i;
            }
            notify();
        }
    }

    public void notifyerAll() throws InterruptedException {
        synchronized (this) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("唤醒");
            for (int i = 0; i < 10; i++) {
                System.out.println(Thread.currentThread() + "notifyer:" + i);
                count += i;
            }
            notifyAll();
        }
    }

    public static class Waiter implements Runnable {
        private Temp temp;

        public Waiter(Temp temp) {
            this.temp = temp;
        }

        public void run() {
            try {
                temp.waiter();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Notifyer implements Runnable {
        private Temp temp;

        public Notifyer(Temp temp) {
            this.temp = temp;
        }

        public void run() {
            try {
                temp.notifyer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class NotifyerAll implements Runnable {
        private Temp temp;

        public NotifyerAll(Temp temp) {
            this.temp = temp;
        }

        public void run() {
            try {
                temp.notifyerAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Temp temp = new Temp();
        for (int x = 1; x < 10; x++) {
            new Thread(new Waiter(temp)).start();
        }

        new Thread(new NotifyerAll(temp)).start();
       /* ExecutorService executorService = Executors.newCachedThreadPool();
        for (int x = 1; x < 10; x++) {
            executorService.execute(new Waiter(temp));
        }
        executorService.execute(new Notifyer(temp));
        executorService.shutdown();*/
    }

}