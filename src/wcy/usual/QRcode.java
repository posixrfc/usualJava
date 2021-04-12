package wcy.usual;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public final class QRcode
{
public static boolean encodeQRcode(String content,String logoPath,int width,int height,String desPath,boolean compress)
{
	Map<EncodeHintType, Object> hints=new HashMap<EncodeHintType,Object>();
	hints.put(EncodeHintType.ERROR_CORRECTION,ErrorCorrectionLevel.H);
	hints.put(EncodeHintType.CHARACTER_SET,"UTF-8");
	hints.put(EncodeHintType.MARGIN,1);
	MultiFormatWriter writer=new MultiFormatWriter();
	BitMatrix matrix=null;
	try{
		matrix=writer.encode(content,BarcodeFormat.QR_CODE,width,height,hints);
	}catch(WriterException e){
		e.printStackTrace(System.err);
	}
	if(null==matrix){
		return false;
	}
	width=matrix.getWidth();
	height=matrix.getHeight();
	BufferedImage bufImg=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
	for(int x=0;x!=width;x++){
		for(int y=0; y!=height;y++){
			bufImg.setRGB(x,y,matrix.get(x,y)?0xFF000000:0xFFFFFFFF);
		}
	}
	if(null==desPath || desPath.length()==0){
		return false;
	}
	File desFile=new File(desPath);
	if(desFile.exists()){
		if(!desFile.delete()){
			return false;
		}
	}else{
		if(!desFile.mkdirs()){
			return false;
		}
		if(!desFile.delete()){
			return false;
		}
	} 
	if(null==logoPath||logoPath.length()==0){
		try{
			return ImageIO.write(bufImg,"PNG",desFile);
		}catch(IOException e){
			e.printStackTrace(System.err);
		}
		return false;
	}
	File logoFile=new File(logoPath);
	if (!logoFile.exists() || !logoFile.isFile() || !logoFile.canRead()){
		try{
			return ImageIO.write(bufImg,"PNG",desFile);
		}catch(IOException e){
			e.printStackTrace(System.err);
		}
		return false;
	}
	Image logoImage;
	try{
		logoImage=ImageIO.read(logoFile);
	}catch (IOException e){
		e.printStackTrace(System.err);
		return false;
	}
	width=logoImage.getWidth(null);
	height=logoImage.getHeight(null);
	if(compress){
		if(width>60){
			width=60;
		}
		if(height>60){
			height=60;
		}
		Image shrinkImg=logoImage.getScaledInstance(width,height,Image.SCALE_SMOOTH);
		BufferedImage tag=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		Graphics g=tag.getGraphics();
		g.drawImage(shrinkImg,0,0,null);
		g.dispose();
		logoImage=shrinkImg;
	}
	Graphics2D g=bufImg.createGraphics();
	int x=(bufImg.getWidth()-width)/2,y=(bufImg.getHeight()-height)/2;
	g.drawImage(logoImage,x,y,width,height,null);
	Shape shape=new RoundRectangle2D.Float(x,y,width,width,6,6);
	g.setStroke(new BasicStroke(3f));
	g.draw(shape);
	g.dispose();
	try{
		return ImageIO.write(bufImg,"PNG",desFile);
	}catch(IOException e){
		e.printStackTrace(System.err);
	}
	return false;
}
public static String decodeQRcode(String path)
{
	File img=new File(path);
	if(!img.exists()){
		return null;
	}
	if(!img.isFile()){
		return null;
	}
	if(!img.canRead()){
		return null;
	}
	BufferedImage bufImg=null;
	try{
		bufImg=ImageIO.read(img);
	}catch (IOException e){
		e.printStackTrace(System.err);
	}
	if(null==bufImg){
		return null;
	}
	LuminanceSource bufSrc=new BufferedImageLuminanceSource(bufImg);
	HybridBinarizer hbrid=new HybridBinarizer(bufSrc);
	BinaryBitmap bitmap=new BinaryBitmap(hbrid);
	Map<DecodeHintType,Object> hints=new HashMap<DecodeHintType,Object>();
	hints.put(DecodeHintType.CHARACTER_SET,"UTF-8");
	MultiFormatReader reader=new MultiFormatReader();
	Result result=null;
	try{
		result=reader.decode(bitmap, hints);
	}catch(NotFoundException e){
		e.printStackTrace(System.err);
	}
	if(null==result){
		return null;
	}
	String value=result.getText();
	return null==value || value.length()==0 ? null : value;
}
private QRcode(){}
}