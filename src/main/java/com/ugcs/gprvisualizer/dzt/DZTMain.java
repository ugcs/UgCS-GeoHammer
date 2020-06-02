package com.ugcs.gprvisualizer.dzt;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import com.ugcs.gprvisualizer.obm.ObjectByteMapper;

public class DZTMain {

	private static void p(String s) {
		System.out.println(s);
	}
	
	public static void main(String[] args) throws Exception {
		
		
		//File file = new File("d:\\work\\georadar\\GPRdata-master\\exampleDataCube\\Grid-dir2-Rawdata\\FILE____019.DZT");
		File file = new File("d:\\work\\georadar\\GPRdata-master\\TEST__001.DZT");
		
		FileInputStream is = new FileInputStream(file);
		FileChannel chan = is.getChannel();
		
		ByteBuffer buf = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN);
		chan.position(0);
		chan.read(buf);
		
		
		/////
		
		ObjectByteMapper obm = new ObjectByteMapper();
		DztHeader header = new DztHeader();
		
		obm.readObject(header, buf);
		
		p(" obm " + header.rh_tag);
		p(" obm " + header.rh_data);
		
		
		////
		
		buf.position(0);
/*		
		int rh_data = 0;
		int rh_nsamp = 0;
		p("rh_tag 	" + buf.getShort());
		p("rh_data 	" + (rh_data = buf.getShort()));
		p("rh_nsamp	" + (rh_nsamp = buf.getShort()));
		p("rh_bits	" + buf.getShort());
		p("rh_zero	" + buf.getShort());
		p("rhf_sps	" + buf.getFloat());
		p("rhf_spm	" + buf.getFloat());
		p("rhf_mpm	" + buf.getFloat());// meters per mark 18
		p("rhf_position	" + buf.getFloat());// position (ns) 22
		p("rhf_range	" + buf.getFloat()); // range (ns) 26
		
		p("rh_npass	" + buf.getShort());
		
		p("create date/time	" + buf.get(4));
		p("modifc date/time	" + buf.get(4));
		
		p("internal	" + buf.getShort());

		p("rh_mapSize " + buf.getShort()); // For internal use		42
		p("rh_text " + buf.getShort()); // offset to text 44
		p("rh_ntext " + buf.getShort()); // size of text 46
		p("rh_proc " + buf.getShort()); // offset to processing history 48
		p("rh_nproc " + buf.getShort()); // size of processing history 50
		p("rh_nchan" + buf.getShort()); // number of channels 52
		
		
		
		int startpos = rh_data;
		FileChannel datachan = is.getChannel();		
		datachan.position(startpos);
		
		*/
//		while (datachan.position() < datachan.size()) {
//			
//			p("---");
//			
//			ByteBuffer databuf = ByteBuffer.allocate(2 * rh_nsamp).order(ByteOrder.LITTLE_ENDIAN);
//			
//			datachan.read(databuf);
//			
//			databuf.position(0);
//			
//			while (databuf.position() < databuf.capacity()) {
//				System.out.print(
//					String.format("%5d ", asUnsignedShort(databuf.getShort()) - 32767));
//			}
//			//p("|||");
//		}
		
		
		
//		short rh_tag; // 0x00ff if header, 0xfnff for old file 00
//		short rh_data; // Offset to Data from beginning of file 02
//		// if rh_data < MINHEADSIZE then
//		// offset is MINHEADSIZE * rh_data
//		// else offset is MINHEADSIZE *rh_nchan
//		
//		short rh_nsamp; // samples per scan 04
//		short rh_bits; // bits per data word (8,16, 32) * 06
//		short rh_zero; // if rh_system is SIR-30 or UtilityScan DF 08
//		// then equals repeats/sample
//		// otherwise is 0x80 for 8 bit data and
//		// 0x8000 for 16 bit data
//		FLOATBYTE(rhf_sps); // scans per second 10
//		FLOATBYTE(rhf_spm); // scans per meter 14
//		FLOATBYTE(rhf_mpm); // meters per mark 18
//		FLOATBYTE(rhf_position); // position (ns) 22
//		FLOATBYTE(rhf_range); // range (ns) 26
		
//		short rh_npass; // num of passes for 2-D files 30
//		RFDATEBYTE(rhb_cdt); // Creation date & time 32
//		RFDATEBYTE(rhb_mdt); // Last modification date & time 36
//		short rh_mapOffset; // For internal use 40 
//		//RADANTM DZT File Format
//		//Geophysical Survey Systems, Inc.
//		//6.14.2016 • Page 3 of 4
//		short rh_mapSize; // For internal use		42
//		short rh_text; // offset to text 44
//		short rh_ntext; // size of text 46
//		short rh_proc; // offset to processing history 48
//		short rh_nproc; // size of processing history 50
//		short rh_nchan; // number of channels 52
		
//		FLOATBYTE(rhf_epsr); // average dielectric constant 54
//		FLOATBYTE(rhf_top); // position in meters 58
//		FLOATBYTE(rhf_depth); // range in meters 62
//		COORDBYTE(rh_coordX); // X coordinates 66
//		FLOATBYTE(rhf_servo_level); // gain servo level 74
//		char reserved[3]; // reserved 78
//		BYTE rh_accomp; // Ant Conf component 81
//		short rh_sconfig; // setup config number 82
//		short rh_spp; // scans per pass 84
//		short rh_linenum; // line number 86
//		COORDBYTE(rh_coordY); // Y coordinates 88
//		BYTE rh_lineorder:4; // 96
//		BYTE rh_slicetype:4; // 96
//		char rh_dtype; // 97
//		char rh_antname[14]; // Antenna name 98
//		BYTE rh_pass0TX:4; // Activ Transmit mask 112
//		BYTE rh_pass1TX:4; // Activ Transmit mask 112
//		BYTE rh_version:3; // 1 – no GPS; 2 - GPS 113
//		BYTE rh_system:5; // (see below for description)** 113
//		char rh_name[12]; // Initial File Name 114
//		short rh_chksum; // checksum for header 126
//		char variable[INFOAREASIZE]; // Variable data 128
//		RGPS rh_RGPS[2]; // GPS info 944 		
		
	}
	
	public static int asUnsignedShort(short s) {
        return s & 0xFFFF;
    }	
}
