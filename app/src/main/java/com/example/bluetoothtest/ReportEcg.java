package com.example.bluetoothtest;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Created by zouyuan on 2017/3/24.
 */

public class ReportEcg {
    static final int fs = 500;

    static double[] ecgin;
    static int[] ain;

    public static double abs(double a)
    {
        if(a<0)
        {
            a=-a;
        }
        return a;
    }

    public static void report() throws Exception
    {
        int numcc=ecgin.length;
        double TotalTime=(double)numcc/fs;
        // TODO: 2017/3/24  
        System.out.println("采集心电数据的总时长为"+TotalTime+"秒");
        System.out.println("=====================================");

        int thR0 = 500;
        int thP1 = 100;
        int thP2 = 50;
        double thPR = 0.08;
        double thQRS = 0.12;
        double thRR = 0.1;
        int thRT0 = 200;

        int e_row=0;					//记录每小时有效数据组数
        int e_col=0;
        int[] e_num=new int[numcc];
        int[] e_begin=new int[numcc];
        for(int i=0;i<(numcc-1);i++)
        {
            if(ain[i]==0)
            {
                e_num[e_row]=e_num[e_row]+1;
            }
            else if(ain[i+1]==0 && ain[i]!=0)//////////////////
            {
                e_row=e_row+1;
                e_begin[e_row]=i+1;
            }
        }

        e_row=e_row+1;
        e_col=e_num[0];
        for(int i=0;i<(e_row-1);i++)
        {
            if(e_num[i+1]>e_col)
            {
                e_col=e_num[i+1];
            }
        }

        int[] ecgin_a_num=new int[e_row];            //每组有效数据个数
        int[] ecgin_a_begin=new int[e_row];          //每组有效数据开始点
        double[] ecgin_a=new double[e_row*e_col];           //每组有效数据
        int ecgin_a_group=e_row;               //每小时有效的数据组数

        for(int i=0;i<e_row;i++)
        {
            ecgin_a_num[i]=e_num[i];
            ecgin_a_begin[i]=e_begin[i];
        }

        for(int j=0;j<e_row;j++)
        {
            for(int i=0;i<ecgin_a_num[j];i++)
            {
                ecgin_a[j*e_col+i]=ecgin[ecgin_a_begin[j]+i];
            }
        }
        int p0=0;
        for(int jj=0;jj<ecgin_a_group;jj++)
        {
            int i=0;
            double max_ecgin_a=abs(ecgin_a[jj*e_col+i]);
            for(int g=1;g<11;g++)
            {
                if(abs(ecgin_a[jj*e_col+i+g])>max_ecgin_a)
                {
                    max_ecgin_a=abs(ecgin_a[jj*e_col+i+g]);
                }
            }
            while(max_ecgin_a>10*thR0 && i<ecgin_a_num[jj]-10-1)
            {
                i++;
                max_ecgin_a=abs(ecgin_a[jj*e_col+i]);
                for(int g=1;g<11;g++)
                {
                    if(abs(ecgin_a[jj*e_col+i+g])>max_ecgin_a)
                    {
                        max_ecgin_a=abs(ecgin_a[jj*e_col+i+g]);
                    }
                }
            }
            int n0=i;
            int n=n0+1;
            while(n>n0 && n<ecgin_a_num[jj])
            {
                if(abs(ecgin_a[jj*e_col+n])<10*thR0 && abs(ecgin_a[jj*e_col+n-1])>=10*thR0)
                {
                    p0++;
                }
                n++;
            }
            p0++;
        }

        int[] ecgin_n_be=new int[p0*2];
        //开始重复，上述为了求p0，定义数组。
        int p=0;
        for(int jj=0;jj<ecgin_a_group;jj++)
        {
            int i=0;
            double max_ecgin_a=abs(ecgin_a[jj*e_col+i]);
            for(int g=1;g<11;g++)
            {
                if(abs(ecgin_a[jj*e_col+i+g])>max_ecgin_a)
                {
                    max_ecgin_a=abs(ecgin_a[jj*e_col+i+g]);
                }
            }
            while(max_ecgin_a>10*thR0 && i<ecgin_a_num[jj]-10-1)
            {
                i++;
                max_ecgin_a=abs(ecgin_a[jj*e_col+i]);
                for(int g=1;g<11;g++)
                {
                    if(abs(ecgin_a[jj*e_col+i+g])>max_ecgin_a)
                    {
                        max_ecgin_a=abs(ecgin_a[jj*e_col+i+g]);
                    }
                }
            }
            int n0=i;
            int n=n0+1;
            ecgin_n_be[p*2+0]=n0+ecgin_a_begin[jj]+1;
            while(n>n0 && n<ecgin_a_num[jj])
            {
                if(abs(ecgin_a[jj*e_col+n])<10*thR0 && abs(ecgin_a[jj*e_col+n-1])>=10*thR0)
                {
                    p++;
                    ecgin_n_be[p*2+0]=n+ecgin_a_begin[jj]+1;
                }
                else if(abs(ecgin_a[jj*e_col+n])>=10*thR0 && abs(ecgin_a[jj*e_col+n-1])<10*thR0)
                {
                    ecgin_n_be[p*2+1]=n-1+ecgin_a_begin[jj]+1;
                }
                if(n==ecgin_a_num[jj]-1)
                {
                    if(abs(ecgin_a[jj*e_col+n])<10*thR0)
                    {
                        ecgin_n_be[p*2+1]=n+ecgin_a_begin[jj]+1;
                    }
                }
                n++;
            }
            p++;
        }
		/*FileWriter writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/ecgin_n_be.txt");
		BufferedWriter bw = new BufferedWriter(writer);
		for(int i = 0; i < ecgin_n_be.length; i++) {
			String str2 = String.valueOf(ecgin_n_be[i]);
			bw.write(str2);
			bw.newLine();
		}
		bw.close();
		writer.close();*/

        ////第一次，计算ecgin_e_num的个数
        int ecgin_e_group=0;
        for(int j=0;j<p;j++)
        {
            if(ecgin_n_be[j*2+1]-ecgin_n_be[j*2+0]>=5000)
            {
                ecgin_e_group++;
            }
        }

        if(ecgin_e_group==0)
        {//// TODO: 2017/3/24
            System.out.println("无有效心电数据");
        }
        if(ecgin_e_group>0)
        {
            ////第二次，求ecgin_e_num的值
            int[] ecgin_e_num=new int[ecgin_e_group];
            int[] ecgin_e_begin=new int[ecgin_e_group];
            int[] ecgin_e_end=new int[ecgin_e_group];
            int m0=0;
            for(int j=0;j<p;j++)
            {
                if(ecgin_n_be[j*2+1]-ecgin_n_be[j*2+0]>=5000)
                {
                    ecgin_e_num[m0]=ecgin_n_be[j*2+1]-ecgin_n_be[j*2+0]-2000+1;
                    ecgin_e_begin[m0]=ecgin_n_be[j*2+0]+1000;
                    ecgin_e_end[m0]=ecgin_n_be[j*2+1]-1000;
                    m0++;
                }
            }
            ////第三次，求ecgin_e_num的最大值
            int max_eenum=ecgin_e_num[0];
            if(ecgin_e_group>1)
            {
                for(int i=1;i<ecgin_e_group;i++)
                {
                    if(ecgin_e_num[i]>max_eenum)
                    {
                        max_eenum=ecgin_e_num[i];
                    }
                }
            }
            ////第四次，求ecgin_e的值
            double[] ecgin_e=new double[ecgin_e_group*max_eenum];
            m0=0;
            for(int j=0;j<p;j++)
            {
                if(ecgin_n_be[j*2+1]-ecgin_n_be[j*2+0]>=5000)
                {
                    for(int i=0;i<ecgin_e_num[m0];i++)//ecgin_e_num[m0]要不要加1呢、、、、、、、、、、、、、、/////////////////////
                    {
                        ecgin_e[m0*max_eenum+i]=ecgin[ecgin_n_be[j*2+0]+1000+i];
                    }
                    m0++;
                }
            }

            FileWriter writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/ecgin_e_num.txt");
            BufferedWriter bw = new BufferedWriter(writer);
            for(int i = 0; i < ecgin_e_num.length; i++) {
                String str2 = String.valueOf(ecgin_e_num[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();
            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/ecgin_e_begin.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < ecgin_e_begin.length; i++) {
                String str2 = String.valueOf(ecgin_e_begin[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();
            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/ecgin_e_end.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < ecgin_e_end.length; i++) {
                String str2 = String.valueOf(ecgin_e_end[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();
            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/ecgin_e.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < ecgin_e.length; i++) {
                String str2 = String.valueOf(ecgin_e[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();


            for(int j=0;j<ecgin_e_group;j++)
            {
                // TODO: 2017/3/24  
                System.out.println("第"+(j+1)+"组有效心电数据起止时间为"+((double)(ecgin_e_begin[j]+1))/fs+"秒--"+((double)(ecgin_e_end[j]+1))/fs+"秒");
            }
            System.out.println("=====================================");

            int rt0=0;
            int thRT=thRT0;
            double thR=thR0;

            ////////////////////////////////////////////111//////////////////////////
            int[] rrt_num=new int[ecgin_e_group];
            int m_max=0;
            for(int j=0;j<ecgin_e_group;j++)
            {
                int[] RT_s={0,0,0,0,0,0,0};
                double[] RM_s={0,0,0,0,0};
                int RT_n=0;
                double RM_n=0;
                int i=0;
                while(i<ecgin_e_num[j]-5*fs-1)
                {
                    while(! ( (ecgin_e[j*max_eenum+i]>thR) && (ecgin_e[j*max_eenum+i+1]>thR) && (ecgin_e[j*max_eenum+i+2]>thR) ) && (i<ecgin_e_num[j]-5*fs-1) )
                    {
                        i=i+1;
                    }
                    int n1=i;
                    while(! ( (ecgin_e[j*max_eenum+i]<thR) && (ecgin_e[j*max_eenum+i+1]<thR) && (ecgin_e[j*max_eenum+i+2]<thR) ) && (i<ecgin_e_num[j]-5*fs-1) )
                    {
                        i=i+1;
                    }
                    int n2=i;
                    int rt1=n1;
                    double rv1=ecgin_e[j*max_eenum+rt1];
                    for(int n=n1;n<n2;n++)
                    {
                        if(ecgin_e[j*max_eenum+n+1]>rv1)
                        {
                            rv1=ecgin_e[j*max_eenum+n+1];
                            rt1=n+1;
                        }
                    }
                    int rt2=0;
                    double rv2=ecgin_e[j*max_eenum+rt1];
                    for(int n=0;n<40;n++)
                    {
                        if(ecgin_e[j*max_eenum+rt1+n+1]<rv2)
                        {
                            rv2=ecgin_e[j*max_eenum+rt1+n+1];
                            rt2=n+1;
                        }
                    }
                    rv2=-1*rv2;
                    int rt;
                    double rv;
                    if(rv2>=thR*0.75)
                    {
                        rt=rt1;
                        rv=rv1;
                    }
                    else
                    {
                        while(! ( (ecgin_e[j*max_eenum+i]>thR) && (ecgin_e[j*max_eenum+i+1]>thR) && (ecgin_e[j*max_eenum+i+2]>thR) ) && (i<ecgin_e_num[j]-5*fs-1) )
                        {
                            i++;
                        }
                        int n3=i;
                        while(! ( (ecgin_e[j*max_eenum+i]<thR) && (ecgin_e[j*max_eenum+i+1]<thR) && (ecgin_e[j*max_eenum+i+2]<thR) ) && (i<ecgin_e_num[j]-5*fs-1) )
                        {
                            i++;
                        }
                        int n4=i;
                        rt1=n3;
                        rv1=ecgin_e[j*max_eenum+rt1];
                        for(int n=n3;n<n4;n++)
                        {
                            if(ecgin_e[j*max_eenum+n+1]>rv1)
                            {
                                rv1=ecgin_e[j*max_eenum+n+1];
                                rt1=n+1;
                            }
                        }
                        rt=rt1;
                        rv=rv1;
                    }

                    if(rrt_num[j]<=5)
                    {
                        rt0=rt;
                    }
                    else
                    {
                        if(rt-rt0>=0.32*(RT_s[1]-RT_s[6]))
                        {
                            i=rt0+thRT;
                            while(! ( (ecgin_e[j*max_eenum+i]>thR/2) && (ecgin_e[j*max_eenum+i+1]>thR/2) && (ecgin_e[j*max_eenum+i+2]>thR/2) ) && (i<rt-thRT) )
                            {
                                i++;
                            }
                            n1=i;
                            while(! ( (ecgin_e[j*max_eenum+i]<thR/2) && (ecgin_e[j*max_eenum+i+1]<thR/2) && (ecgin_e[j*max_eenum+i+2]<thR/2) ) && (i<rt-thRT) )
                            {
                                i++;
                            }
                            n2=i;
                            if(n1<rt-thRT && n2<rt-thRT)
                            {
                                rt1=n1;
                                rv1=ecgin_e[j*max_eenum+rt1];
                                for(int n=n1;n<n2;n++)
                                {
                                    if(ecgin_e[j*max_eenum+n+1]>rv1)
                                    {
                                        rv1=ecgin_e[j*max_eenum+n+1];
                                        rt1=n+1;
                                    }
                                }
                                rt2=0;
                                rv2=ecgin_e[j*max_eenum+rt1];
                                for(int n=0;n<40;n++)
                                {
                                    if(ecgin_e[j*max_eenum+rt1+n+1]<rv2)
                                    {
                                        rv2=ecgin_e[j*max_eenum+rt1+n+1];
                                        rt2=n+1;
                                    }
                                }
                                rv2=-1*rv2;
                                if(rv2>=thR*0.75)
                                {
                                    rt=rt1;
                                    rv=rv1;
                                }
                                else
                                {
                                    while(! ( (ecgin_e[j*max_eenum+i]>thR/2) && (ecgin_e[j*max_eenum+i+1]>thR/2) && (ecgin_e[j*max_eenum+i+2]>thR/2) ) && (i<ecgin_e_num[j]-5*fs-1) )
                                    {
                                        i++;
                                    }
                                    n1=i;
                                    while(! ( (ecgin_e[j*max_eenum+i]<thR/2) && (ecgin_e[j*max_eenum+i+1]<thR/2) && (ecgin_e[j*max_eenum+i+2]<thR/2) ) && (i<ecgin_e_num[j]-5*fs-1) )
                                    {
                                        i++;
                                    }
                                    n2=i;
                                    rt1=n1;
                                    rv1=ecgin_e[j*max_eenum+rt1];
                                    for(int n=n1;n<n2;n++)
                                    {
                                        if(ecgin_e[j*max_eenum+n+1]>rv1)
                                        {
                                            rv1=ecgin_e[j*max_eenum+n+1];
                                            rt1=n+1;
                                        }
                                    }
                                    rt=rt1;
                                    rv=rv1;
                                }
                            }
                        }
                        rt0=rt;
                    }
                    ////////////////////////////////rt的值不再改变///////////////////////////////////////////////
                    if((rt+1>=fs) && (rt+1<=ecgin_e_num[j]-5*fs))
                    {
                        RM_n=rv;
                        RT_n=rt+ecgin_e_begin[j];
                        for(int n=6;n>0;n--)
                        {
                            RT_s[n]=RT_s[n-1];
                        }
                        for(int n=4;n>0;n--)
                        {
                            RM_s[n]=RM_s[n-1];
                        }
                        RT_s[0]=RT_n;
                        RM_s[0]=RM_n;
                        if(rrt_num[j]>=5)
                        {
                            thRT=(int)((RT_s[0]-RT_s[5])/7.5);
                            if(thRT>fs*1.5)
                            {
                                thRT=thRT0;
                            }
                            thR=(RM_s[0]+RM_s[1]+RM_s[2]+RM_s[3]+RM_s[4])/7.5;
                        }
                        else
                        {
                            thRT=thRT0;
                            thR=thR0;
                        }
                        rrt_num[j]++;
                    }
                    i=rt+thRT;
                }
            }
            m_max=rrt_num[0];

            for(int i=0;i<(ecgin_e_group-1);i++)
            {
                if(rrt_num[i+1]>m_max)
                {
                    m_max=rrt_num[i+1];
                }
            }

            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/rrt_num.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < rrt_num.length; i++) {
                String str2 = String.valueOf(rrt_num[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();

            //////////////////////////////////////222//////////////////////////
            rt0=0;
            thRT=thRT0;
            thR=thR0;

            double[] PM=new double[ecgin_e_group*m_max];
            double[] PRT=new double[ecgin_e_group*m_max];
            double[] RM=new double[ecgin_e_group*m_max];
            int[] RT=new int[ecgin_e_group*m_max];
            double[] QRST=new double[ecgin_e_group*m_max];
            double[] TM=new double[ecgin_e_group*m_max];
            double[] RRT=new double[ecgin_e_group*m_max];
            double[] RRT_dif=new double[ecgin_e_group*m_max];

            int sxzb=0;
            int sxxdgs=0;
            int ssxzb=0;
            int ssxxdgs=0;
            int flag1=0;
            int flag2=0;

            for(int j=0;j<ecgin_e_group;j++)
            {
                int i=0;
                int k=0;
                while(i<ecgin_e_num[j]-5*fs-1)
                {
                    while(! ( (ecgin_e[j*max_eenum+i]>thR) && (ecgin_e[j*max_eenum+i+1]>thR) && (ecgin_e[j*max_eenum+i+2]>thR) ) && (i<ecgin_e_num[j]-5*fs-1) )
                    {
                        i=i+1;
                    }
                    int n1=i;
                    while(! ( (ecgin_e[j*max_eenum+i]<thR) && (ecgin_e[j*max_eenum+i+1]<thR) && (ecgin_e[j*max_eenum+i+2]<thR) ) && (i<ecgin_e_num[j]-5*fs-1) )
                    {
                        i=i+1;
                    }
                    int n2=i;
                    int rt1=n1;
                    double rv1=ecgin_e[j*max_eenum+rt1];
                    for(int n=n1;n<n2;n++)
                    {
                        if(ecgin_e[j*max_eenum+n+1]>rv1)
                        {
                            rv1=ecgin_e[j*max_eenum+n+1];
                            rt1=n+1;
                        }
                    }
                    int rt2=0;
                    double rv2=ecgin_e[j*max_eenum+rt1];
                    for(int n=0;n<40;n++)
                    {
                        if(ecgin_e[j*max_eenum+rt1+n+1]<rv2)
                        {
                            rv2=ecgin_e[j*max_eenum+rt1+n+1];
                            rt2=n+1;
                        }
                    }
                    rv2=-1*rv2;
                    int rt;
                    double rv;
                    if(rv2>=thR*0.75)
                    {
                        rt=rt1;
                        rv=rv1;
                    }
                    else
                    {
                        while(! ( (ecgin_e[j*max_eenum+i]>thR) && (ecgin_e[j*max_eenum+i+1]>thR) && (ecgin_e[j*max_eenum+i+2]>thR) ) && (i<ecgin_e_num[j]-5*fs-1) )
                        {
                            i++;
                        }
                        int n3=i;
                        while(! ( (ecgin_e[j*max_eenum+i]<thR) && (ecgin_e[j*max_eenum+i+1]<thR) && (ecgin_e[j*max_eenum+i+2]<thR) ) && (i<ecgin_e_num[j]-5*fs-1) )
                        {
                            i++;
                        }
                        int n4=i;
                        rt1=n3;
                        rv1=ecgin_e[j*max_eenum+rt1];
                        for(int n=n3;n<n4;n++)
                        {
                            if(ecgin_e[j*max_eenum+n+1]>rv1)
                            {
                                rv1=ecgin_e[j*max_eenum+n+1];
                                rt1=n+1;
                            }
                        }
                        rt=rt1;
                        rv=rv1;
                    }

                    if(k<=5)
                    {
                        rt0=rt;
                    }
                    else
                    {
                        if(rt-rt0>=0.32*(RT[j*m_max+k-1]-RT[j*m_max+k-6]))
                        {
                            i=rt0+thRT;
                            while(! ( (ecgin_e[j*max_eenum+i]>thR/2) && (ecgin_e[j*max_eenum+i+1]>thR/2) && (ecgin_e[j*max_eenum+i+2]>thR/2) ) && (i<rt-thRT) )
                            {
                                i++;
                            }
                            n1=i;
                            while(! ( (ecgin_e[j*max_eenum+i]<thR/2) && (ecgin_e[j*max_eenum+i+1]<thR/2) && (ecgin_e[j*max_eenum+i+2]<thR/2) ) && (i<rt-thRT) )
                            {
                                i++;
                            }
                            n2=i;
                            if(n1<rt-thRT && n2<rt-thRT)
                            {
                                rt1=n1;
                                rv1=ecgin_e[j*max_eenum+rt1];
                                for(int n=n1;n<n2;n++)
                                {
                                    if(ecgin_e[j*max_eenum+n+1]>rv1)
                                    {
                                        rv1=ecgin_e[j*max_eenum+n+1];
                                        rt1=n+1;
                                    }
                                }
                                rt2=0;
                                rv2=ecgin_e[j*max_eenum+rt1];
                                for(int n=0;n<40;n++)
                                {
                                    if(ecgin_e[j*max_eenum+rt1+n+1]<rv2)
                                    {
                                        rv2=ecgin_e[j*max_eenum+rt1+n+1];
                                        rt2=n+1;
                                    }
                                }
                                rv2=-1*rv2;
                                if(rv2>=thR*0.75)
                                {
                                    rt=rt1;
                                    rv=rv1;
                                }
                                else
                                {
                                    while(! ( (ecgin_e[j*max_eenum+i]>thR/2) && (ecgin_e[j*max_eenum+i+1]>thR/2) && (ecgin_e[j*max_eenum+i+2]>thR/2) ) && (i<ecgin_e_num[j]-5*fs-1) )
                                    {
                                        i++;
                                    }
                                    n1=i;
                                    while(! ( (ecgin_e[j*max_eenum+i]<thR/2) && (ecgin_e[j*max_eenum+i+1]<thR/2) && (ecgin_e[j*max_eenum+i+2]<thR/2) ) && (i<ecgin_e_num[j]-5*fs-1) )
                                    {
                                        i++;
                                    }
                                    n2=i;
                                    rt1=n1;
                                    rv1=ecgin_e[j*max_eenum+rt1];
                                    for(int n=n1;n<n2;n++)
                                    {
                                        if(ecgin_e[j*max_eenum+n+1]>rv1)
                                        {
                                            rv1=ecgin_e[j*max_eenum+n+1];
                                            rt1=n+1;
                                        }
                                    }
                                    rt=rt1;
                                    rv=rv1;
                                }
                            }
                        }
                        rt0=rt;
                    }
                    ////////////////////////////////rt的值不再改变///////////////////////////////////////////////
                    if((rt+1>=fs) && (rt+1<=ecgin_e_num[j]-5*fs))
                    {
                        int qt=rt-35;
                        double qv=ecgin_e[j*max_eenum+qt];
                        for(int n=rt-35;n<rt-5;n++)
                        {
                            if(ecgin_e[j*max_eenum+n+1]<qv)
                            {
                                qv=ecgin_e[j*max_eenum+n+1];
                                qt=n+1;
                            }
                        }
                        qt=qt-10;

                        int st=rt+5;
                        double sv=ecgin_e[j*max_eenum+st];
                        for(int n=rt+5;n<rt+35;n++)
                        {
                            if(ecgin_e[j*max_eenum+n+1]<sv)
                            {
                                sv=ecgin_e[j*max_eenum+n+1];
                                st=n+1;
                            }
                        }
                        st=st-10;

                        int pt1=rt-80;
                        double pv1=ecgin_e[j*max_eenum+pt1];
                        for(int n=rt-80;n<rt-40;n++)
                        {
                            if(ecgin_e[j*max_eenum+n+1]>pv1)
                            {
                                pv1=ecgin_e[j*max_eenum+n+1];
                                pt1=n+1;
                            }
                        }
                        PM[j*m_max+k]=pv1;
                        int pt=pt1;

                        int tt1=rt+40;
                        double tv1=ecgin_e[j*max_eenum+tt1];
                        double tv2=ecgin_e[j*max_eenum+tt1];
                        for(int n=rt+40;n<rt+150;n++)
                        {
                            if(ecgin_e[j*max_eenum+n+1]>tv1)
                            {
                                tv1=ecgin_e[j*max_eenum+n+1];
                            }
                            if(ecgin_e[j*max_eenum+n+1]<tv2)
                            {
                                tv2=ecgin_e[j*max_eenum+n+1];
                            }
                        }

                        if(tv1>(-1)*tv2)
                        {
                            TM[j*m_max+k]=tv1;
                        }
                        else
                        {
                            TM[j*m_max+k]=tv2;
                        }

                        PRT[j*m_max+k]=(double)(rt-pt)/fs;
                        RM[j*m_max+k]=rv;
                        RT[j*m_max+k]=rt+ecgin_e_begin[j]+1;
                        QRST[j*m_max+k]=(double)(st-qt)/fs;

                        if(k>=1)
                        {
                            RRT[j*m_max+k-1]=60/((double)(RT[j*m_max+k]-RT[j*m_max+k-1])/fs);
                        }
                        if(k>=5)
                        {
                            thRT=(int)((RT[j*m_max+k]-RT[j*m_max+k-5])/7.5);
                            if(thRT>fs*1.5)
                            {
                                thRT=thRT0;
                            }
                            thR=(RM[j*m_max+k]+RM[j*m_max+k-1]+RM[j*m_max+k-2]+RM[j*m_max+k-3]+RM[j*m_max+k-4])/7.5;
                        }
                        else
                        {
                            thRT=thRT0;
                            thR=thR0;
                        }


                        if(k>=2)
                        {
                            RRT_dif[j*m_max+k-2]=abs(60/RRT[j*m_max+k-1]-60/RRT[j*m_max+k-2]);
                        }
                        if(k>=5)
                        {
                            if((PM[j*m_max+k]<=thP2) && (QRST[j*m_max+k]>thQRS) && (TM[j*m_max+k]<=0))
                            {
                                sxzb=sxzb+1;              //室性早搏次数
                            }


                            if((PM[j*m_max+k]<=thP2) && (QRST[j*m_max+k]<thQRS) && (RRT[j*m_max+k-1]>40) && (RRT[j*m_max+k-1]<60))
                            {
                                ssxzb=ssxzb+1;              //室上性早搏次数
                            }


                            if((PM[j*m_max+k]<=thP2) && (QRST[j*m_max+k]>thQRS) && (QRST[j*m_max+k-1]>thQRS) && (QRST[j*m_max+k-2]>thQRS) && (TM[j*m_max+k]<=0) && (RRT[j*m_max+k-1]>100))
                            {
                                if(flag1==0)
                                {
                                    sxxdgs=sxxdgs+1;          //室性心动过速次数
                                    flag1=1;
                                }
                                else
                                {
                                    flag1=1;
                                }
                            }
                            else
                            {
                                flag1=0;
                            }
                            if((PM[j*m_max+k]<=thP2) && (QRST[j*m_max+k]<thQRS) && (QRST[j*m_max+k-1]<thQRS) && (QRST[j*m_max+k-2]<thQRS) && (RRT[j*m_max+k-1]>160))
                            {
                                if(flag2==0)
                                {
                                    ssxxdgs=ssxxdgs+1;          //室上性心动过速次数
                                    flag2=1;
                                }
                                else
                                {
                                    flag2=1;
                                }
                            }
                            else
                            {
                                flag2=0;
                            }
                        }
                        k++;
                    }
                    i=rt+thRT;
                }
            }

            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/PM.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < PM.length; i++) {
                String str2 = String.valueOf(PM[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();
            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/PRT.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < PRT.length; i++) {
                String str2 = String.valueOf(PRT[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();
            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/RM.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < RM.length; i++) {
                String str2 = String.valueOf(RM[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();
            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/RT.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < RT.length; i++) {
                String str2 = String.valueOf(RT[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();
            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/QRST.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < QRST.length; i++) {
                String str2 = String.valueOf(QRST[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();
            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/TM.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < TM.length; i++) {
                String str2 = String.valueOf(TM[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();
            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/RRT.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < RRT.length; i++) {
                String str2 = String.valueOf(RRT[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();
            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/RRT_dif.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < RRT_dif.length; i++) {
                String str2 = String.valueOf(RRT_dif[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();

            //////////////////////////////////////////333/////////////////////////
            double[] Tsxzb=new double[sxzb];
            double[] Tssxzb=new double[ssxzb];
            int[] sxxdgs_rrt=new int[sxxdgs];
            double[] tsxxdgs_begin=new double[sxxdgs];
            double[] tsxxdgs_end=new double[sxxdgs];
            int[] ssxxdgs_rrt=new int[ssxxdgs];
            double[] tssxxdgs_begin=new double[ssxxdgs];
            double[] tssxxdgs_end=new double[ssxxdgs];

            sxzb=0;
            sxxdgs=0;
            ssxzb=0;
            ssxxdgs=0;
            flag1=0;
            flag2=0;

            for(int j=0;j<ecgin_e_group;j++)
            {
                int i=0;
                int k=0;
                while(i<ecgin_e_num[j]-5*fs-1)
                {
                    while(! ( (ecgin_e[j*max_eenum+i]>thR) && (ecgin_e[j*max_eenum+i+1]>thR) && (ecgin_e[j*max_eenum+i+2]>thR) ) && (i<ecgin_e_num[j]-5*fs-1) )
                    {
                        i=i+1;
                    }
                    int n1=i;
                    while(! ( (ecgin_e[j*max_eenum+i]<thR) && (ecgin_e[j*max_eenum+i+1]<thR) && (ecgin_e[j*max_eenum+i+2]<thR) ) && (i<ecgin_e_num[j]-5*fs-1) )
                    {
                        i=i+1;
                    }
                    int n2=i;
                    int rt1=n1;
                    double rv1=ecgin_e[j*max_eenum+rt1];
                    for(int n=n1;n<n2;n++)
                    {
                        if(ecgin_e[j*max_eenum+n+1]>rv1)
                        {
                            rv1=ecgin_e[j*max_eenum+n+1];
                            rt1=n+1;
                        }
                    }
                    int rt2=0;
                    double rv2=ecgin_e[j*max_eenum+rt1];
                    for(int n=0;n<40;n++)
                    {
                        if(ecgin_e[j*max_eenum+rt1+n+1]<rv2)
                        {
                            rv2=ecgin_e[j*max_eenum+rt1+n+1];
                            rt2=n+1;
                        }
                    }
                    rv2=-1*rv2;
                    int rt;
                    double rv;
                    if(rv2>=thR*0.75)
                    {
                        rt=rt1;
                        rv=rv1;
                    }
                    else
                    {
                        while(! ( (ecgin_e[j*max_eenum+i]>thR) && (ecgin_e[j*max_eenum+i+1]>thR) && (ecgin_e[j*max_eenum+i+2]>thR) ) && (i<ecgin_e_num[j]-5*fs-1) )
                        {
                            i++;
                        }
                        int n3=i;
                        while(! ( (ecgin_e[j*max_eenum+i]<thR) && (ecgin_e[j*max_eenum+i+1]<thR) && (ecgin_e[j*max_eenum+i+2]<thR) ) && (i<ecgin_e_num[j]-5*fs-1) )
                        {
                            i++;
                        }
                        int n4=i;
                        rt1=n3;
                        rv1=ecgin_e[j*max_eenum+rt1];
                        for(int n=n3;n<n4;n++)
                        {
                            if(ecgin_e[j*max_eenum+n+1]>rv1)
                            {
                                rv1=ecgin_e[j*max_eenum+n+1];
                                rt1=n+1;
                            }
                        }
                        rt=rt1;
                        rv=rv1;
                    }

                    if(k<=5)
                    {
                        rt0=rt;
                    }
                    else
                    {
                        if(rt-rt0>=0.32*(RT[j*m_max+k-1]-RT[j*m_max+k-6]))
                        {
                            i=rt0+thRT;
                            while(! ( (ecgin_e[j*max_eenum+i]>thR/2) && (ecgin_e[j*max_eenum+i+1]>thR/2) && (ecgin_e[j*max_eenum+i+2]>thR/2) ) && (i<rt-thRT) )
                            {
                                i++;
                            }
                            n1=i;
                            while(! ( (ecgin_e[j*max_eenum+i]<thR/2) && (ecgin_e[j*max_eenum+i+1]<thR/2) && (ecgin_e[j*max_eenum+i+2]<thR/2) ) && (i<rt-thRT) )
                            {
                                i++;
                            }
                            n2=i;
                            if(n1<rt-thRT && n2<rt-thRT)
                            {
                                rt1=n1;
                                rv1=ecgin_e[j*max_eenum+rt1];
                                for(int n=n1;n<n2;n++)
                                {
                                    if(ecgin_e[j*max_eenum+n+1]>rv1)
                                    {
                                        rv1=ecgin_e[j*max_eenum+n+1];
                                        rt1=n+1;
                                    }
                                }
                                rt2=0;
                                rv2=ecgin_e[j*max_eenum+rt1];
                                for(int n=0;n<40;n++)
                                {
                                    if(ecgin_e[j*max_eenum+rt1+n+1]<rv2)
                                    {
                                        rv2=ecgin_e[j*max_eenum+rt1+n+1];
                                        rt2=n+1;
                                    }
                                }
                                rv2=-1*rv2;
                                if(rv2>=thR*0.75)
                                {
                                    rt=rt1;
                                    rv=rv1;
                                }
                                else
                                {
                                    while(! ( (ecgin_e[j*max_eenum+i]>thR/2) && (ecgin_e[j*max_eenum+i+1]>thR/2) && (ecgin_e[j*max_eenum+i+2]>thR/2) ) && (i<ecgin_e_num[j]-5*fs-1) )
                                    {
                                        i++;
                                    }
                                    n1=i;
                                    while(! ( (ecgin_e[j*max_eenum+i]<thR/2) && (ecgin_e[j*max_eenum+i+1]<thR/2) && (ecgin_e[j*max_eenum+i+2]<thR/2) ) && (i<ecgin_e_num[j]-5*fs-1) )
                                    {
                                        i++;
                                    }
                                    n2=i;
                                    rt1=n1;
                                    rv1=ecgin_e[j*max_eenum+rt1];
                                    for(int n=n1;n<n2;n++)
                                    {
                                        if(ecgin_e[j*max_eenum+n+1]>rv1)
                                        {
                                            rv1=ecgin_e[j*max_eenum+n+1];
                                            rt1=n+1;
                                        }
                                    }
                                    rt=rt1;
                                    rv=rv1;
                                }
                            }
                        }
                        rt0=rt;
                    }
                    ////////////////////////////////rt的值不再改变///////////////////////////////////////////////
                    if((rt+1>=fs) && (rt+1<=ecgin_e_num[j]-5*fs))
                    {
                        if(k>=5)
                        {
                            thRT=(int)((RT[j*m_max+k]-RT[j*m_max+k-5])/7.5);
                            if(thRT>fs*1.5)
                            {
                                thRT=thRT0;
                            }
                            thR=(RM[j*m_max+k]+RM[j*m_max+k-1]+RM[j*m_max+k-2]+RM[j*m_max+k-3]+RM[j*m_max+k-4])/7.5;
                        }
                        else
                        {
                            thRT=thRT0;
                            thR=thR0;
                        }

                        if(k>=5)
                        {
                            if((PM[j*m_max+k]<=thP2) && (QRST[j*m_max+k]>thQRS) && (TM[j*m_max+k]<=0))
                            {
                                sxzb=sxzb+1;              //室性早搏次数
                                Tsxzb[sxzb-1]=RT[j*m_max+k];
                                //// TODO: 2017/3/24
                                System.out.println("室性早搏发生时间在第"+Tsxzb[sxzb-1]/fs+"秒");
                            }


                            if((PM[j*m_max+k]<=thP2) && (QRST[j*m_max+k]<thQRS) && (RRT[j*m_max+k-1]>40) && (RRT[j*m_max+k-1]<60))
                            {
                                ssxzb=ssxzb+1;              //室上性早搏次数
                                Tssxzb[ssxzb-1]=RT[j*m_max+k];
                                //// TODO: 2017/3/24
                                System.out.println("室上性早搏发生时间在第"+Tssxzb[ssxzb-1]/fs+"秒");
                            }


                            if((PM[j*m_max+k]<=thP2) && (QRST[j*m_max+k]>thQRS) && (QRST[j*m_max+k-1]>thQRS) && (QRST[j*m_max+k-2]>thQRS) && (TM[j*m_max+k]<=0) && (RRT[j*m_max+k-1]>100))
                            {
                                if(flag1==0)
                                {
                                    sxxdgs=sxxdgs+1;          //室性心动过速次数
                                    sxxdgs_rrt[sxxdgs-1]=(int)(RRT[j*m_max+k-1]);
                                    tsxxdgs_begin[sxxdgs-1]=RT[j*m_max+k-2];
                                    tsxxdgs_end[sxxdgs-1]=RT[j*m_max+k];
                                    flag1=1;
                                }
                                else
                                {
                                    tsxxdgs_end[sxxdgs-1]=RT[j*m_max+k];
                                    flag1=1;
                                }
                            }
                            else
                            {
                                flag1=0;
                            }


                            if((PM[j*m_max+k]<=thP2) && (QRST[j*m_max+k]<thQRS) && (QRST[j*m_max+k-1]<thQRS) && (QRST[j*m_max+k-2]<thQRS) && (RRT[j*m_max+k-1]>160))
                            {
                                if(flag2==0)
                                {
                                    ssxxdgs=ssxxdgs+1;          //室上性心动过速次数
                                    ssxxdgs_rrt[ssxxdgs-1]=(int)(RRT[j*m_max+k-1]);
                                    tssxxdgs_begin[ssxxdgs-1]=RT[j*m_max+k-2];
                                    tssxxdgs_end[ssxxdgs-1]=RT[j*m_max+k];
                                    flag2=1;
                                }
                                else
                                {
                                    tssxxdgs_end[ssxxdgs-1]=RT[j*m_max+k];
                                    flag2=1;
                                }
                            }
                            else
                            {
                                flag2=0;
                            }
                        }
                        k++;
                    }
                    i=rt+thRT;
                }
            }


            int rrt_num_h=0;
            for(int j=0;j<ecgin_e_group;j++)
            {
                if(rrt_num[j]>=1)
                {
                    for(int n=0;n<rrt_num[j]-1;n++)
                    {
                        rrt_num_h++;
                    }
                }
            }

            int m=0;
            double[] RRT_h=new double[rrt_num_h];
            int[] RRT_ht=new int[rrt_num_h];
            double[] RRT_dif_h=new double[rrt_num_h];
            double[] PM_h=new double[rrt_num_h];
            double[] PRT_h=new double[rrt_num_h];
            double[] QRST_h=new double[rrt_num_h];
            for(int j=0;j<ecgin_e_group;j++)
            {
                if(rrt_num[j]>=1)
                {
                    for(int n=0;n<rrt_num[j]-1;n++)
                    {
                        RRT_h[m]=RRT[j*m_max+n];
                        RRT_ht[m]=RT[j*m_max+n];
                        RRT_dif_h[m]=RRT_dif[j*m_max+n];
                        PM_h[m]=PM[j*m_max+n];
                        PRT_h[m]=PRT[j*m_max+n];
                        QRST_h[m]=QRST[j*m_max+n];
                        m++;
                    }
                }
            }

            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/PM_h.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < PM_h.length; i++) {
                String str2 = String.valueOf(PM_h[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();
            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/PRT_h.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < PRT_h.length; i++) {
                String str2 = String.valueOf(PRT_h[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();
            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/RRT_h.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < RRT_h.length; i++) {
                String str2 = String.valueOf(RRT_h[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();
            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/QRST_h.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < QRST_h.length; i++) {
                String str2 = String.valueOf(QRST_h[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();
            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/RRT_ht.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < RRT_ht.length; i++) {
                String str2 = String.valueOf(RRT_ht[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();
            writer = new FileWriter("/Users/zouyuan/ZYWORK/MyASProject/data/RRT_dif_h.txt");
            bw = new BufferedWriter(writer);
            for(int i = 0; i < RRT_dif_h.length; i++) {
                String str2 = String.valueOf(RRT_dif_h[i]);
                bw.write(str2);
                bw.newLine();
            }
            bw.close();
            writer.close();


            //计算心率
            if(rrt_num_h<12)
            {//// TODO: 2017/3/24
                System.out.println("无有效心电数据");
            }
            else
            {
                double RRT_max=RRT_h[5];
                double RRT_min=RRT_h[5];
                double RRT_avg=RRT_h[5];
                double PM_avg=PM_h[5];
                double RRT_mm=RRT_dif[5];
                double PRT_avg=PRT_h[5];
                double QRST_avg=QRST_h[5];

                int maxt=5;
                int mint=5;
                for(int i=6;i<rrt_num_h-6;i++)
                {
                    if(RRT_h[i]>RRT_max)
                    {
                        RRT_max=RRT_h[i];
                        maxt=i;
                    }
                    if(RRT_h[i]<RRT_min)
                    {
                        RRT_min=RRT_h[i];
                        mint=i;
                    }
                    RRT_avg=RRT_avg+RRT_h[i];
                    RRT_mm=RRT_mm+RRT_dif[i];
                    PM_avg=PM_avg+PM_h[i];
                    PRT_avg=PRT_avg+PRT_h[i];
                    QRST_avg=QRST_avg+QRST_h[i];
                }
                int RRT_maxt=RRT_ht[maxt];
                int RRT_mint=RRT_ht[mint];
                RRT_avg=RRT_avg/(rrt_num_h-11);
                RRT_mm=RRT_mm/(rrt_num_h-11);
                PM_avg=PM_avg/(rrt_num_h-11);
                PRT_avg=PRT_avg/(rrt_num_h-11);
                QRST_avg=QRST_avg/(rrt_num_h-11);


                //检测窦性心律
                // TODO: 2017/3/24

                if((PM_avg>=thP1) && (PRT_avg>=thPR) && (RRT_mm>=thRR))
                {
                    System.out.println("诊断为窦性心律不齐");
                }
                else if((PM_avg>=thP1) && (PRT_avg>=thPR) && (RRT_avg>=100))
                {
                    System.out.println("诊断为窦性心动过速");
                }
                else if((PM_avg>=thP1) && (PRT_avg>=thPR) && (RRT_avg<=60))
                {
                    System.out.println("诊断为窦性心动过缓");
                }
                else if((PM_avg>=thP1) && (PRT_avg>=thPR))
                {
                    System.out.println("诊断为正常窦性心律");
                }
                else if((PM_avg<=thP2) && (QRST_avg<=thQRS) && (RRT_avg>=100))
                {
                    System.out.println("诊断为心房颤动");
                }
                else
                {
                    System.out.println("诊断为非窦性心律");
                }
// TODO: 2017/3/24
                System.out.println("平均心率为"+(int)(RRT_avg)+"次/每分钟");
                System.out.println("最大心率为"+(int)(RRT_max)+"次/每分钟，发生在第"+(double)(RRT_maxt/fs)+"秒");
                System.out.println("最小心率为"+(int)(RRT_min)+"次/每分钟，发生在第"+(double)(RRT_mint/fs)+"秒");

                int mm=0;
                for(int i=5;i<rrt_num_h-6;i++)
                {
                    if(RRT_h[i]<=(RRT_avg/1.3)||RRT_h[i]>=RRT_avg*1.3)
                    {
                        mm++;
                    }
                }
                int[] yc=new int[mm];
                mm=0;
                for(int i=5;i<rrt_num_h-6;i++)
                {
                    if(RRT_h[i]<=(RRT_avg/1.3)||RRT_h[i]>=RRT_avg*1.3)
                    {
                        yc[mm]=i;
                        mm++;
                    }
                }
                // TODO: 2017/3/24
                System.out.println("发生异常心率波形为"+mm+"次");

                System.out.println("发生室性早搏次数为"+sxzb+"次");
                System.out.println("发生室上性早搏次数为"+ssxzb+"次");
                System.out.println("发生室性心动过速次数为"+sxxdgs+"次");
                System.out.println("发生室上性心动过速次数为"+ssxxdgs+"次");

                if(sxxdgs>=1)
                {
                    for(int i=0;i<sxxdgs;i++)
                    {
                        //// TODO: 2017/3/24
                        System.out.println("第"+i+"次室性心动过速,开始时间在"+(double)(tsxxdgs_begin[i]/fs)+"秒,持续时间为"+(double)(tsxxdgs_end[i]-tsxxdgs_begin[i])/fs+"秒,心率为"+sxxdgs_rrt[i]+"次/每分钟");
                    }
                }

                if(ssxxdgs>=1)
                {
                    for(int i=0;i<ssxxdgs;i++)
                    {// TODO: 2017/3/24
                        System.out.println("第"+i+"次室上性心动过速,开始时间在"+(double)(tssxxdgs_begin[i]/fs)+"秒,持续时间为"+(double)(tssxxdgs_end[i]-tssxxdgs_begin[i])/fs+"秒,心率为"+ssxxdgs_rrt[i]+"次/每分钟");
                    }
                }
            }
        }// TODO: 2017/3/24
        System.out.println("=====================================");
    };
}
