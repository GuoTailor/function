import com.gyh.api.DeviceStatus;
import com.gyh.function.StaticConf;

public class Test {
    public static void main(String[] args) {
        StaticConf conf = StaticConf.getInstance();
        conf.addDeviceStatus(new DeviceStatus("12", StaticConf.online, 12L), 50_000, null);
        conf.addDeviceStatus(new DeviceStatus("13", StaticConf.online, 12L), 50_000, null);
        conf.addDeviceStatus(new DeviceStatus("14", StaticConf.online, 12L), 50_000, null);
        conf.addDeviceStatus(new DeviceStatus("15", StaticConf.online, 12L), 50_000, null);
        conf.forEachRemove("12", deviceStatus -> System.out.println(deviceStatus.content));
        //System.out.println(conf.deviceStatuses.size());
        conf.addDeviceStatus(new DeviceStatus("12", StaticConf.offline, 12L), 50_000, null);
        //System.out.println(conf.deviceStatuses.size());
    }
}
