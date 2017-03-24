package com.example.bluetoothtest;

/**
 * Created by zouyuan on 2017/3/24.
 */

public class ThreeFilter {
    static final int fs = 500;

    static int[] wecg;
    static double[] wecgfirdata;
    static int[] wecg2a;

    //public static void filter();

    public static void filter()
    {
        int numcc = wecg.length;
        int[] wecg2 = new int[numcc];
        int[] wecga = new int[numcc];
        for(int i=0;i<numcc;i++)
        {
            wecg2[i] = (wecg[i]>>1)-16384;
            wecga[i] = wecg[i] & 0x0001;
        }

        //低通滤波
        double[] b1={0.007337,0.009406,0.015408,0.024760,0.036549,0.049622,0.062699,0.074499,0.083865,0.089880,0.091952,0.089880,0.083865,0.074499,0.062699,0.049622,0.036549,0.024760,0.015408,0.009406,0.007337};
        double[] firnodata=new double[numcc];
        for(int n=0;n<numcc;n++)
        {
            double ZZ=0;
            for(int i=0;i<21;i++)
            {
                if(n-i+1>0)
                {
                    ZZ=ZZ+b1[i]*wecg2[n-i];
                }
            }
            firnodata[n]=ZZ;
        }
        //工频50Hz陷波滤波器
        double[] fir50data=new double[numcc];
        double[] b={1,-1.618033988749895,1};
        double[] a={1,-1.456230589874906,0.81};
        for(int n=0;n<numcc;n++)
        {
            double XX=0;
            double YY=0;
            for(int i=0;i<3;i++)
            {
                if(n-i+1>0)
                {
                    XX=XX+b[i]*firnodata[n-i];
                }
            }
            for(int i=1;i<3;i++)
            {
                if(n-i+1>0)
                {
                    YY=YY+a[i]*fir50data[n-i];
                }
            }
            fir50data[n]=XX-YY;
        }
        //运动伪差SG滤波器
        double sgg[]=new double[51];
        numcc=numcc-26;
        double[] firsgdata=new double[numcc];
        for(int i=0;i<51;i++)
        {
            sgg[i]=0.019607843137255;
        }
        for(int n=0;n<25;n++)
        {
            firsgdata[n]=0;
        }
        for(int n=25;n<numcc;n++)
        {
            double X=0;
            for(int i=0;i<51;i++)
            {
                X=X+sgg[i]*fir50data[n-25+i];
            }
            firsgdata[n]=X;
        }
        for(int n=0;n<numcc;n++)
        {
            firsgdata[n]=fir50data[n]-firsgdata[n];
        }
        //低通滤波
        double[] firlowdata=new double[numcc];
        for(int n=0;n<numcc;n++)
        {
            double ZZ=0;
            for(int i=0;i<21;i++)
            {
                if(n-i+1>0)
                {
                    ZZ=ZZ+b1[i]*firsgdata[n-i];
                }
            }
            firlowdata[n]=ZZ;
        }

        int num = numcc-5*fs+1;
        wecgfirdata=new double[num];
        wecg2a=new int[num];
        for(int n=5*fs-1;n<numcc;n++)
        {
            if(firlowdata[n]>=16)
            {
                wecgfirdata[n-5*fs+1]=16384;
                wecg2a[n-5*fs+1]=1;
            }
            else if(firlowdata[n]<=-16)
            {
                wecgfirdata[n-5*fs+1]=-16384;
                wecg2a[n-5*fs+1]=1;
            }
            else
            {
                wecgfirdata[n-5*fs+1]=firlowdata[n]*1024;
                wecg2a[n-5*fs+1]=wecga[n-fs];
            }
        }
    };
}
