package no.jonkjenn;

import tinyb.*;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TempService {
    private static final float SCALE_LSB = 0.03125f;
    static boolean running = true;

    private class Characteristics{
        private final BluetoothGattCharacteristic writeableChar;
        private final BluetoothGattCharacteristic notifiableChar;
        private final BluetoothGattCharacteristic clientChar;

        public Characteristics(BluetoothGattCharacteristic writeableChar, BluetoothGattCharacteristic notifiableChar, BluetoothGattCharacteristic clientChar){
            this.writeableChar = writeableChar;
            this.notifiableChar = notifiableChar;
            this.clientChar = clientChar;
        }
    }

    static void printDevice(BluetoothDevice device) {
        System.out.print("Address = " + device.getAddress());
        System.out.print(" Name = " + device.getName());
        System.out.print(" Connected = " + device.getConnected());
        System.out.println();
    }

    static float convertCelsius(int raw) {
        return raw / 128f;
    }

    /*
     * After discovery is started, new devices will be detected. We can get a list of all devices through the manager's
     * getDevices method. We can the look through the list of devices to find the device with the MAC which we provided
     * as a parameter. We continue looking until we find it, or we try 15 times (1 minutes).
     */
    static BluetoothDevice getDevice(String address) throws InterruptedException {
        BluetoothManager manager = BluetoothManager.getBluetoothManager();
        BluetoothDevice sensor = null;
        for (int i = 0; (i < 15) && running; ++i) {
            List<BluetoothDevice> list = manager.getDevices();
            if (list == null)
                return null;

            for (BluetoothDevice device : list) {
                printDevice(device);
                /*
                 * Here we check if the address matches.
                 */
                if (device.getAddress().equals(address))
                    sensor = device;
            }

            if (sensor != null) {
                return sensor;
            }
            Thread.sleep(4000);
        }
        return null;
    }

    static Collection<BluetoothGattService> getServices(BluetoothDevice device) throws InterruptedException {
        System.out.println("Servicess exposed by device:");
        List<BluetoothGattService> bluetoothServices = null;
        do {
            bluetoothServices = device.getServices();
            if (bluetoothServices == null)
                return Collections.EMPTY_LIST;

            Thread.sleep(1000);
        } while (bluetoothServices.isEmpty() && running);
        return bluetoothServices;
    }

    static BluetoothGattService getService(BluetoothDevice device, String UUID) throws InterruptedException {
        System.out.println("Services exposed by device:");
        BluetoothGattService tempService = null;
        List<BluetoothGattService> bluetoothServices = null;
        do {
            bluetoothServices = device.getServices();
            if (bluetoothServices == null)
                return null;

            for (BluetoothGattService service : bluetoothServices) {
                System.out.println("UUID: " + service.getUUID());
                if (service.getUUID().equalsIgnoreCase(UUID))
                    tempService = service;
            }
            Thread.sleep(4000);
        } while (bluetoothServices.isEmpty() && running);
        return tempService;
    }

    static BluetoothGattCharacteristic getCharacteristic(BluetoothGattService service, String UUID) {
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        if (characteristics == null)
            return null;

        for (BluetoothGattCharacteristic characteristic : characteristics) {
            if (characteristic.getUUID().equals(UUID))
                return characteristic;
        }
        return null;
    }

    /*
     * This program connects to a TI SensorTag 2.0 and reads the temperature characteristic exposed by the device over
     * Bluetooth Low Energy. The parameter provided to the program should be the MAC address of the device.
     *
     * A wiki describing the sensor is found here: http://processors.wiki.ti.com/index.php/CC2650_SensorTag_User's_Guide
     *
     * The API used in this example is based on TinyB v0.3, which only supports polling, but v0.4 will introduce a
     * simplied API for discovering devices and services.
     */
    public static void main(String[] args) throws InterruptedException {

        if (args.length < 1) {
            System.err.println("Run with <device_address> argument");
            System.exit(-1);
        }

        /*
         * To start looking of the device, we first must initialize the TinyB library. The way of interacting with the
         * library is through the BluetoothManager. There can be only one BluetoothManager at one time, and the
         * reference to it is obtained through the getBluetoothManager method.
         */
        BluetoothManager manager = BluetoothManager.getBluetoothManager();

        /*
         * The manager will try to initialize a BluetoothAdapter if any adapter is present in the system. To initialize
         * discovery we can call startDiscovery, which will put the default adapter in discovery mode.
         */
        boolean discoveryStarted = manager.startDiscovery();

        System.out.println("The discovery started: " + (discoveryStarted ? "true" : "false"));
        BluetoothDevice sensor = getDevice(args[0]);

        /*
         * After we find the device we can stop looking for other devices.
         */
        try {
            manager.stopDiscovery();
        } catch (BluetoothException e) {
            System.err.println("Discovery could not be stopped.");
        }

        if (sensor == null) {
            System.err.println("No sensor found with the provided address.");
            System.exit(-1);
        }

        System.out.print("Found device: ");
        printDevice(sensor);

        if (sensor.connect())
            System.out.println("Sensor with the provided address connected");
        else {
            System.out.println("Could not connect device.");
            //System.exit(-1);
        }

        final Lock lock = new ReentrantLock();
        final Condition cv = lock.newCondition();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                running = false;
                lock.lock();
                try {
                    cv.signalAll();
                } finally {
                    lock.unlock();
                }

            }
        });

        //String serviceUUID = "00002902-0000-1000-8000-00805f9b34fb";
//        String SERVICE_UUID = "00001801-0000-1000-8000-00805f9b34fb";
//        String TEMP_VALUE = "fe552080-4180-8a02-ef2c-1b42a0ac3f83";
//        String TEMP_CONFIG = "fe552081-4180-8a02-ef2c-1b42a0ac3f83";
//        String TEMP_PERIOD = "fe552082-4180-8a02-ef2c-1b42a0ac3f83";

        String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
        String NOTIFIABLE_CHARACTERISTIC_UUID = "FE552082-4180-8A02-EF2C-1B42A0AC3F83";
        String SERVICE_UUID = "FE552080-4180-8A02-EF2C-1B42A0AC3F83";
        String SOME_UUID = "00001801-0000-1000-8000-00805f9b34fb";
        String WRITABLE_CHARACTERISTIC_UUID = "FE552081-4180-8A02-EF2C-1B42A0AC3F83";

        Collection<BluetoothGattService> services = getServices(sensor);

        BluetoothGattService service = services.stream().filter( s -> s.getUUID().equalsIgnoreCase(SERVICE_UUID)).findFirst().get();

        List<BluetoothGattCharacteristic> chars = service.getCharacteristics();

        BluetoothGattCharacteristic writeChar = chars.stream().filter( c -> c.getUUID().equalsIgnoreCase(WRITABLE_CHARACTERISTIC_UUID)).findFirst().get();
        BluetoothGattCharacteristic notifyChar = chars.stream().filter( c -> c.getUUID().equalsIgnoreCase(NOTIFIABLE_CHARACTERISTIC_UUID)).findFirst().get();

//        List<BluetoothGattDescriptor> notifyDescriptors = notifyChar.getDescriptors();
//        BluetoothGattDescriptor notfiyDescriptor = notifyDescriptors.stream().filter( d -> d.getUUID().equalsIgnoreCase(CLIENT_CHARACTERISTIC_CONFIG)).findFirst().get();
//        notfiyDescriptor.writeValue(BluetoothGattDescriptor)

        notifyChar.enableValueNotifications(new BluetoothNotification<byte[]>() {
            @Override
            public void run(byte[] bytes) {
                System.out.println("Temp: " +((int)bytes[2] & 0xff));
                System.out.println("Humidity: " + ((int)bytes[3] & 0xff));
            }
        });

        System.out.println("Found the temperature characteristics");

        /*
         * Turn on the Temperature Service by writing 1 in the configuration characteristic, as mentioned in the PDF
         * mentioned above. We could also modify the update interval, by writing in the period characteristic, but the
         * default 1s is good enough for our purposes.
         */
        //byte[] config = { 0x01 };
        //tempConfig.writeValue(config);

        /*
         * Each second read the value characteristic and display it in a human readable format.
         */
        while (running) {
//            byte[] tempRaw = notifiableChar.readValue();
//            System.out.print("Temp raw = {");
//            for (byte b : tempRaw) {
//                System.out.print(String.format("%02x,", b));
//            }
//            System.out.print("}");

            /*
             * The temperature service returns the data in an encoded format which can be found in the wiki. Convert the
             * raw temperature format to celsius and print it. Conversion for object temperature depends on ambient
             * according to wiki, but assume result is good enough for our purposes without conversion.
             */
            //int objectTempRaw = (tempRaw[0] & 0xff) | (tempRaw[1] << 8);
            //int ambientTempRaw = (tempRaw[2] & 0xff) | (tempRaw[3] << 8);

            ////float objectTempCelsius = convertCelsius(objectTempRaw);
            //float ambientTempCelsius = convertCelsius(ambientTempRaw);

            //System.out.println(
                    //String.format(" Temp: Object = %fC, Ambient = %fC", objectTempCelsius, ambientTempCelsius));

            lock.lock();
            try {
                cv.await(1, TimeUnit.SECONDS);
            } finally {
                lock.unlock();
            }
        }
        sensor.disconnect();

    }
}
