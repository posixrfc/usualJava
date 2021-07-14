package wcy.usual;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

public final class QRCodes
{
public static boolean encodeQRCode(CharSequence content,int width,int height,OutputStream retos,CharSequence fmt)
{
	RenderedImage bufImg=generateRenderedImage(content,width,height,BufferedImage.TYPE_INT_RGB);
	try{
		return ImageIO.write(bufImg,fmt.toString(),retos);
	}catch(IOException e){
		e.printStackTrace(System.err);
	}
	return false;
}
public static boolean encodeQRCode(CharSequence content,int width,int height,OutputStream retos,CharSequence fmt,InputStream logois,int logow,int logoh)
{
	RenderedImage bufImg=generateRenderedImage(content,width,height,BufferedImage.TYPE_INT_RGB);
	Image logoImage;
	try{
		logoImage=ImageIO.read(logois);
	}catch (IOException e){
		e.printStackTrace(System.err);
		return false;
	}
	width=logoImage.getWidth(null);
	height=logoImage.getHeight(null);
	if(width!=logow && height!=logoh){
		Image shrinkImg=logoImage.getScaledInstance(logow,logoh,Image.SCALE_SMOOTH);
		BufferedImage tmpImg=new BufferedImage(logow,logoh,BufferedImage.TYPE_INT_RGB);
		Graphics g=tmpImg.getGraphics();
		g.drawImage(shrinkImg,0,0,null);
		g.dispose();
		logoImage=shrinkImg;
	}
	Graphics2D g=((BufferedImage)bufImg).createGraphics();
	int x=(bufImg.getWidth()-logow)/2,y=(bufImg.getHeight()-logoh)/2;
	g.drawImage(logoImage,x,y,logow,logoh,null);
	Shape shape=new RoundRectangle2D.Float(x,y,logow,logoh,6,6);
	g.setStroke(new BasicStroke(3f));
	g.draw(shape);
	g.dispose();
	try{
		return ImageIO.write(bufImg,fmt.toString(),retos);
	}catch(IOException e){
		e.printStackTrace(System.err);
	}
	return false;
}
public static RenderedImage generateRenderedImage(CharSequence content,int width,int height,int type)
{
	Map<EncodeHintType, Object> hints=new HashMap<EncodeHintType,Object>();
	hints.put(EncodeHintType.ERROR_CORRECTION,ErrorCorrectionLevel.H);
	hints.put(EncodeHintType.CHARACTER_SET,"UTF-8");
	hints.put(EncodeHintType.MARGIN,1);
	MultiFormatWriter writer=new MultiFormatWriter();
	BitMatrix matrix=null;
	try{
		matrix=writer.encode(content.toString(),BarcodeFormat.QR_CODE,width,height,hints);
	}catch(WriterException e){
		e.printStackTrace(System.err);
	}
	if(null==matrix){
		return null;
	}
	width=matrix.getWidth();
	height=matrix.getHeight();
	BufferedImage bufImg=new BufferedImage(width,height,type);
	for(int x=0;x!=width;x++){
		for(int y=0; y!=height;y++){
			bufImg.setRGB(x,y,matrix.get(x,y)?0xff000000:0xffffffff);
		}
	}
	return bufImg;
}
public static String decodeQRCode(InputStream imgis)
{
	if(null==imgis){
		return null;
	}
	BufferedImage bufImg=null;
	try{
		bufImg=ImageIO.read(imgis);
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
protected QRCodes(){}
}