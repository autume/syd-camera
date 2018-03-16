package com.oden.syd_camera.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class SDCardUtils {

    public final static String DIR_SINGLE_SDCARD_NAME = "内置存储卡";
    public final static String DIR_SDCARD_NAME = "内置存储卡";
    public final static String DIR_EXT_SDCARD_NAME = "扩展存储卡";

    private static String SDCARD_PATH = getSDCardPath();

    // 系统目录链接，key是linkPath，value是realPath
    public static HashMap<String, String> sysLinkToReal = new HashMap<String, String>();
    // 系统目录的反向链接， key是realPath，value是linkPath
    public static HashMap<String, String> sysRealToLink = new HashMap<String, String>();

    /**
     * 获得内置sd卡剩余容量，即可用大小，单位M
     * @return
     */
    public static long getInnerSDAvailableSize(Context context) {
        ArrayList<SDCardStat> sdCardStatList = SDCardUtils.getSDCardStats(context);
        if (sdCardStatList.size() > 0) {
            return sdCardStatList.get(0).freeSize/1024/1024;
        }else{
            return 0;
        }
    }

    /**
     * 获得内置sd卡已用容量 单位M
     * @return
     */
    public static long getInnerSDUsedSize(Context context) {
        ArrayList<SDCardStat> sdCardStatList = SDCardUtils.getSDCardStats(context);
        if (sdCardStatList.size() > 0) {
            SDCardStat sdCardStat = sdCardStatList.get(0);
            return (sdCardStat.totalSize - sdCardStat.freeSize)/1024/1024;
        }else{
            return 0;
        }
    }

    /**
     * 获得sd卡剩余容量，即可用大小，单位M
     * @return
     */
    public static long getSDAvailableSize(Context context) {
        ArrayList<SDCardStat> sdCardStatList = SDCardUtils.getSDCardStats(context);
        if (sdCardStatList.size() > 0) {
            return sdCardStatList.get(sdCardStatList.size() - 1).freeSize/1024/1024;
        }else{
            return 0;
        }
    }

    /**
     * 获得sd卡已用容量 单位M
     * @return
     */
    public static long getSDUsedSize(Context context) {
        ArrayList<SDCardStat> sdCardStatList = SDCardUtils.getSDCardStats(context);
        if (sdCardStatList.size() > 0) {
            SDCardStat sdCardStat = sdCardStatList.get(sdCardStatList.size() - 1);
            return (sdCardStat.totalSize - sdCardStat.freeSize)/1024/1024;
        }else{
            return 0;
        }
    }

//        ArrayList<SDCardUtils.SDCardStat> SdCardStatList = SDCardUtils.getSDCardStats(this);
//        for(SDCardUtils.SDCardStat sdCardStat:SdCardStatList){
//            L.d("SD卡路径：" + sdCardStat.rootPath + "\n");
//            L.d("SD卡总空间：" + sdCardStat.totalSize/1024/1024/1024 + "G"+"\n");
//            L.d("SD卡可用空间：" + sdCardStat.freeSize/1024/1024/1024 +"G"+ "\n");
//        }

    public static boolean isMounted() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * 是否能通过基本过滤
     * 
     * @param str
     * @return
     */
    private static boolean testBasicFilter(String str) {
        String[] keys = new String[] { "sd", "emmc", "hwuserdata", "udisk",
                "ext_card", "usbotg", "disk1", "disk2", "disk3", "disk4",
                "usbdrivea", "usbdriveb", "usbdrivec", "usbdrived", "storage",
                "external" };
        int len = keys.length;
        for (int i = 0; i < len; i++) {
            if (str.contains(keys[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean sdCardCanWrite(String path) {
        if (path == null) {
            return false;
        }

        File SdCardRoot = new File(path);
        if (SdCardRoot.canWrite() == false) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= 19) {
            // canWrite() 在4.4系统不起作用，只要路径存在总是返回true
            File testPath = new File(new File(path), ".testwrite"
                    + String.valueOf(System.currentTimeMillis()));

            if (testPath.mkdirs()) {
                testPath.delete();
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static ArrayList<SDCardStat> getSDCardStats(Context context) {
        ArrayList<SDCardStat> list = new ArrayList<SDCardStat>();

        try {
            Process process = Runtime.getRuntime().exec("mount");
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            // BufferedReader br = new BufferedReader(new InputStreamReader(new
            // FileInputStream("/mnt/sdcard/sdcards_info/mount.log")));
            String str, lowerStr;
            while ((str = br.readLine()) != null) {
                lowerStr = str.toLowerCase();
//                Log.d("gaolei", "str---------line-----"+str);
                if(str.contains("/storage/")){
//                	 MainActivity.filterBuilder.append("\nstorage:\n");
//                    MainActivity.filterBuilder.append("storage----:  "+str+"\n\n");
                }
//                if(str.contains("/sd/")){
//                	MainActivity.filterBuilder.append("sd:\n\n");
//                	MainActivity.filterBuilder.append(str+"\n\n");
//                }
                // Utils.writeLog("getSDCardStats: " + lowerStr);
                if (!testBasicFilter(lowerStr)) {
                    continue;
                }
                String[] cols = str.split("\\s+");
                if (cols == null) {
                    continue;
                }

                String path = findSDCardPath(cols);
                Log.d("gaolei", "path--------0-------"+path);
                if (TextUtils.isEmpty(path)) {
                    // path = findUDiskPath(cols);
                    // if (!TextUtils.isEmpty(path)) {
                    // SDCardStat.Format format = findSDCardFormat(cols);
                    // if (format != null) {
                    // UDiskStat = new SDCardStat(path,
                    // format, 1025);
                    // }
                    // }
                    continue;
                }
                SDCardStat.Format format = findSDCardFormat(cols);
                if (format == null) {
                    continue;
                }
                
                int minorIdx = (SDCardStat.Format.vfat == format || SDCardStat.Format.exfat == format || SDCardStat.Format.texfat == format) ? findVoldDevNodeMinorIndex(cols)
                        : -100;
                SDCardStat stat = new SDCardStat(path, format, minorIdx);
                Log.d("gaolei", "path--------1-------"+path);
				if (!compareData(list, stat.totalSize)) {
					continue;
				}
                
//                MainActivity.filterBuilder.append("path----:  "+path+"\n\n");
                // 4.4以上版本修改trootPath路径，因为4.4及以上版本不支持往外置SD卡根目录写权限
                if (Build.VERSION.SDK_INT >= 19) {
                    if (!SDCardUtils.sdCardCanWrite(path)) {
                        stat.canWrite = false;
                        File[] filePath = ContextCompat.getExternalFilesDirs(
                                context, null);
                        if (filePath != null) {
                            for (File f : filePath) {
                                if (f != null) {
                                    if (f.getAbsolutePath().startsWith(path)) {
                                        stat.rootPath = f.getAbsolutePath();
                                        Log.d("gaolei", "path--------if-------"+path);
//                                        MainActivity.filterBuilder.append("path--if--:  "+path+"\n\n");
                                        list.add(stat);
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                    	Log.d("gaolei", "path--------else-------"+path);
//                    	 MainActivity.filterBuilder.append("path--else--:  "+path+"\n\n");
                        list.add(stat);
                    }
                } else {
//                	MainActivity.filterBuilder.append("path--other--:  "+path+"\n\n");
                	Log.d("gaolei", "path--------other-------"+path);
                    list.add(stat);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return list;
        }

        list = sortSDCardList(list);

        for (int idx = 0, size = list.size(); idx < size; idx++) {
            if (idx == 0) {
                list.get(0).name = (size == 1) ? DIR_SINGLE_SDCARD_NAME
                        : DIR_SDCARD_NAME;
            } else if (idx == 1) {
                list.get(1).name = DIR_EXT_SDCARD_NAME;
            } else {
                list.get(idx).name = DIR_EXT_SDCARD_NAME + idx;
            }
        }

        // 将U盘放最后
        // if (UDiskStat != null) {
        // UDiskStat.name = DIR_UDISK_NAME;
        // list.add(UDiskStat);
        // }

        return list;
    }

    private static boolean checkSDCardExistByRootPath(String path,
            ArrayList<SDCardStat> list) {
        boolean existFlag = false;
        for (SDCardStat stat : list) {
            if (SDCARD_PATH.equals(stat.rootPath)) {
                existFlag = true;
                break;
            }
        }
        return existFlag;
    }

    /**
     * 根据设备挂载次序排序SDCard
     * 
     * @param list
     * @return
     */
    private static ArrayList<SDCardStat> sortSDCardList(
            ArrayList<SDCardStat> list) {
        ArrayList<SDCardStat> resultList = new ArrayList<SDCardStat>();
        int minIdx = 0;
        for (SDCardStat stat : list) {
            if (minIdx == 0) {
                resultList.add(stat);
                minIdx = stat.voldMinorIdx;
                continue;
            }

            if (stat.voldMinorIdx < minIdx
                    || isInnerSdcard(stat.rootPath, stat.totalSize)) {
                resultList.add(0, stat);
                minIdx = stat.voldMinorIdx;
            } else {
                resultList.add(stat);
            }
        }
        return resultList;
    }

    private final static long SD_PHY_SIZE_1G = 1000 * 1000 * 1000;
    private final static long SD_LOGIC_SIZE_1G = 1024 * 1024 * 1024;
    private final static double SD_LOGIC_DIFF = SD_LOGIC_SIZE_1G
            / (double) SD_PHY_SIZE_1G;

    static boolean nCF3(int n) {
        boolean boo = true;

        String s = Integer.toBinaryString(n);

        byte[] b = s.getBytes();

        for (int i = 1; i < b.length; i++) {
            if (b[i] != 48) {
                boo = false;
                break;
            }
        }

        return boo;
    }

    private static boolean isPhySize(long totalSize) {
        boolean result = false;
        long count = totalSize / SD_PHY_SIZE_1G;
        if (count % 2 == 0) {
            count = count + 0;
        } else {
            count = count + 1;
        }
        if (!nCF3((int) count) || 0 >= totalSize) {
            return result;
        }
        double real_diff = SD_LOGIC_SIZE_1G * count / (double) totalSize;
        // 1.063 <= real_diff <= 1.083
        result = real_diff >= SD_LOGIC_DIFF - 0.01
                && real_diff <= SD_LOGIC_DIFF + 0.01;
        return result;
    }

    private static boolean isInnerSdcard(String path, long totalSize) {
        try {
            if (!path.endsWith("/")) {
                path = path + "/";
            }
            return !isPhySize(totalSize)
                    && (Environment.getExternalStorageDirectory()
                            .getAbsoluteFile().getCanonicalPath() + "/")
                            .equals(path);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 根据mount信息解析sdcard路径
     * 
     * @param mountInfo
     * @return
     */
    private static String findSDCardPath(String[] mountInfo) {
        String lowerStr;
        for (String col : mountInfo) {
            lowerStr = col.toLowerCase();
//            Log.d("gaolei", "lowerStr--------------"+lowerStr);
            // lenovo 部分手机会把扩展卡bind镜像 /mnt/extrasd_bind
           
            if ((lowerStr.contains("sd") && !lowerStr.contains("extrasd_bind"))
                    || lowerStr.contains("emmc")
                    || lowerStr.contains("ext_card")
                    || lowerStr.contains("external_sd")
                    || lowerStr.contains("usbstorage")) {
                String pDir = getParentPath(col);
                // onda平板 扩展卡 /mnt/sdcard/external_sdcard, 三星note扩展卡
                // /mnt/sdcard/external_sd
                // Sony C6603 扩展卡 /storage/removable/sdcard1
                if (pDir.equals(getParentPath(SDCARD_PATH))
                        || pDir.equals(SDCARD_PATH)
                        || pDir.equals(SDCARD_PATH + "/")
                        || pDir.equals("/storage/")
                        || pDir.equals("/storage/removable/")) {
                    return col;
                }
            }
            
            if ((col.contains("/storage/") && !col.contains("self") && !col
					.contains("legacy"))) {
            	Log.d("gaolei", "storage--------------"+col);
				return col;
			}
            if (col.equals("/mnt/ext_sdcard")) {
                // 华为p6扩展卡
                return col;
            }
            if (col.equals("/udisk")) {
                // coolpad 内置卡 /udisk
                return col;
            }
            if (col.equals("/HWUserData")) {
                // 部分 huawei 内置卡 /HWUserData
                return col;
            }
            if (col.equals("/storage/external")) {
                // coolpad8720l 外置卡
                return col;
            }
            if (col.equals("/Removable/MicroSD")) {
                // ASUS_T00G
                return col;
            }
        }
        return null;
    }

    /**
     * 获取SD卡显示路径.<br>
     * 类似<strong>/storage/emulated/0</strong>不需要显示路径.<br>
     * 类似<strong>/storage/extSdCard/Android/data/{pageageName}/files</strong>
     * 只显示从<strong>/Android</strong>开头的路径.
     * 
     * @return
     */
    public static String getShowSDPath(SDCardStat stat) {
        String showPath = "";
        String path = stat.rootPath;
        if (Build.VERSION.SDK_INT >= 19 && !stat.canWrite) {
            int index = path.indexOf("Android/data/");
            if (index != -1) {
                showPath = path.substring(index);
            }
        } else {
            showPath = path.substring(path.lastIndexOf(File.separator) + 1);
            if (showPath.equals("0")) {
                showPath = "";
            }
        }
        return showPath;
    }

    /**
     * 根据mount信息解析U盘路径
     * 
     * @param mountInfo
     * @return
     */
    // private static String findUDiskPath(String[] mountInfo) {
    // /*
    // * MI2 /storage/udisk (U盘不会挂载到根目录 （coolpad 内置卡 /udisk）) MC002
    // * /mnt/usbotg GT-I9220 /mnt/sdcard/usbStorage/UsbDriveA GT-I9500
    // * /storage/UsbDriveA M040 /data/system/scsi/Disk1 (魅族随机出Disk1，2，3，4)
    // */
    //
    // String lowerStr;
    // String[] keys = new String[] {
    // "udisk", "usbotg", "disk1", "disk2",
    // "disk3", "disk4", "usbdrivea", "usbdriveb", "usbdrivec",
    // "usbdrived"
    // };
    // int keyLen = keys.length;
    // for (String col : mountInfo) {
    // lowerStr = col.toLowerCase();
    // for (int i = 0; i < keyLen; i++) {
    // if (lowerStr.contains(keys[i])) {
    // return col;
    // }
    // }
    // }
    // return null;
    // }

    /**
     * 根据mount信息解析sdcard分区格式
     * 
     * @param mountInfo
     * @return
     */
    private static SDCardStat.Format findSDCardFormat(String[] mountInfo) {
        int formatMinLength = 0;
        int formatMaxLength = 0;
        for (SDCardStat.Format format : SDCardStat.Format.values()) {
            int len = format.toString().length();
            if (len > formatMaxLength) {
                formatMaxLength = len;
            } else if (len < formatMinLength) {
                formatMinLength = len;
            }
        }

        for (String col : mountInfo) {
            if (col.length() < formatMinLength
                    || col.length() > formatMaxLength) {
                continue;
            }
            for (SDCardStat.Format format : SDCardStat.Format.values()) {
                if (format.toString().equals(col)) {
                    return format;
                }
            }
        }
        return null;
    }

    /**
     * 解析Vold设备号
     * 
     * @param mountInfo
     * @return
     */
    private static String findVoldDevNodeIndex(String[] mountInfo) {
        if (mountInfo == null || mountInfo.length <= 0) {
            return null;
        }

        String voldInfo = mountInfo[0];
        if (TextUtils.isEmpty(voldInfo)) {
            return null;
        }

        return voldInfo.replaceFirst("/dev/block/vold/", "");
    }

    /**
     * 解析Vold(vfat格式)次设备号
     * 
     * @param mountInfo
     * @return
     */
    private static int findVoldDevNodeMinorIndex(String[] mountInfo) {
        String voldInfo = findVoldDevNodeIndex(mountInfo);
        if (TextUtils.isEmpty(voldInfo)) {
            return -1;
        }

        String[] infos = voldInfo.split(":");
        if (infos == null || infos.length < 2) {
            return -1;
        }
        return Integer.valueOf(infos[1]);
    }

    /**
     * 计算目标路径的磁盘使用情况
     * 
     * @param path
     * @return
     */
    private static DiskStat getDiskCapacity(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        StatFs stat = new StatFs(path);
        long blockSize = stat.getBlockSize();
        long totalBlockCount = stat.getBlockCount();
        long feeBlockCount = stat.getAvailableBlocks();
        return new DiskStat(blockSize * feeBlockCount, blockSize
                * totalBlockCount);
    }

    /**
     * 取上一级路径
     * 
     * @param path
     * @return
     */
    public static String getParentPath(String path) {
        if (path != null && path.length() > 0) {
            path = path.substring(0, path.length() - 1); // 去掉最后一个字符 ， 以兼容以“/”
                                                         // 结尾的路径
            return path.substring(0, path.lastIndexOf(File.separator) + 1);
        } else {
            return "";
        }
    }
 // 1.判断如果总容量小于2G,则排除 2.排除内置或外置重复路径
 	public static boolean compareData(ArrayList<SDCardStat> list,
 			long capacity) {
 		//排除内置或外置重复路径
 		if (list.size() > 0) {
 			for (int i = 0; i < list.size(); i++) {
 				if (list.get(i).totalSize==capacity) {
 					Log.d("gaolei", "duplicate-------------------------");
 					return false;
 					
 				}
 			}

 		}
 		//判断如果总容量小于2G
 		if (capacity/ 1073741824<2) {
 			Log.d("gaolei", "capacity/ 1073741824-------------------------"+capacity/ 1073741824);
 				return false;
 		}
 		return true;
 	}

    public static class SDCardStat {

        public String rootPath;
        public String excludePath; // 排除路径， 某些手机会将扩展卡挂载在sdcard下面
        public String name;
        public Format format;
        // public long usedSize;
        public long totalSize;
        public long freeSize;
        public boolean isCaseSensitive;
        public int voldMinorIdx;
        // voldMinorIdx 小于此值的为sdcard内置设备，大于此值的是u盘
        public static int SDCARD_MAX_COUNT = 1024;
        /**
         * Android4.4增加了SD卡读写权限设置，分为内置存储和外置SD卡，对权限见下表：<br>
         * <table width="60%" border="1" align="center">
         * <tr>
         * <th align="center">Action</th>
         * <th align="center">Primary</th>
         * <th align="center">Secondary</th>
         * </tr>
         * <tbody>
         * <tr>
         * <td>Read Top-Level Directories</td>
         * <td align="center">R</td>
         * <td align="center">R</td>
         * </tr>
         * <tr>
         * <td>Write Top-Level Directories</td>
         * <td align="center">W</td>
         * <td align="center">N</td>
         * </tr>
         * <tr>
         * <td>Read My Package&#8217;s Android Data Directory</td>
         * <td align="center">Y</td>
         * <td align="center">Y</td>
         * </tr>
         * <tr>
         * <td>Write My Package&#8217;s Android Data Directory</td>
         * <td align="center">Y</td>
         * <td align="center">Y</td>
         * </tr>
         * <tr>
         * <td>Read Another Package&#8217;s Android Data Directory</td>
         * <td align="center">R</td>
         * <td align="center">R</td>
         * </tr>
         * <tr>
         * <td>Write Another Package&#8217;s Android Data Directory</td>
         * <td align="center">W</td>
         * <td align="center">N</td>
         * </tr>
         * </tbody>
         * </table>
         * <p style="text-align: center;">
         * <strong>R = With Read Permission, W = With Write Permission, Y =
         * Always, N = Never </strong>
         * </p>
         * 根据上面表格判断SD类型，这个属性代表了Write Top-Level Directories的Secondary(外置SD卡).<br>
         * 由于部分手机厂商没有遵循Google新的SD卡规范，所以在部分Android4.4手机上外置SD卡的根目录仍然有读写
         * 权限.所以只有在Android4.4以上手机，并且外置SD卡不可写的情况此属性才为<strong>false</strong>.
         */
        public boolean canWrite = true;

        public SDCardStat(String path, Format format, int voldMinorIdx,
                String excludePath) {
            DiskStat stat = getDiskCapacity(path);
            if (stat != null) {
                this.freeSize = stat.free;
                this.totalSize = stat.total;
            }
            this.rootPath = path;
            this.format = format;
            this.isCaseSensitive = checkCaseSensitive(format);
            // this.name = getSDCardName(path);
            this.voldMinorIdx = voldMinorIdx;
            this.excludePath = excludePath;
        }

        public SDCardStat(String path, Format format, int voldMinorIdx) {
            this(path, format, voldMinorIdx, "");
        }

        public static enum Format {
            vfat, exfat, ext4, fuse, sdcardfs, texfat
        }

        public boolean checkCaseSensitive(Format format) {
            return (format == Format.vfat || format == Format.exfat) ? false
                    : true;
        }

        public void setExcludePath(String excludePath) {
            DiskStat excludeStat = getDiskCapacity(excludePath);
            if (excludeStat != null) {
                this.freeSize -= excludeStat.free;
                this.totalSize -= excludeStat.total;

            }
            this.excludePath = excludePath;
        }

        public void refreshDiskCapacity() {
            DiskStat stat = getDiskCapacity(this.rootPath);
            if (stat != null) {
                this.freeSize = stat.free;
                this.totalSize = stat.total;

            }
        }
    }

    public static class DiskStat {
        public long free;
        public long total;

        public DiskStat(long free, long total) {
            this.free = free;
            this.total = total;
        }
    }

}
