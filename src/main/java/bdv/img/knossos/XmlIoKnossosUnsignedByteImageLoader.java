package bdv.img.knossos;

import java.io.File;

import org.jdom2.Element;

import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.ImgLoaderIo;
import mpicbg.spim.data.generic.sequence.XmlIoBasicImgLoader;

@ImgLoaderIo( format = "knossos", type = KnossosUnsignedByteImageLoader.class )
public class XmlIoKnossosUnsignedByteImageLoader implements XmlIoBasicImgLoader< KnossosUnsignedByteImageLoader >
{
	@Override
	public Element toXml( final KnossosUnsignedByteImageLoader imgLoader, final File basePath )
	{
		throw new UnsupportedOperationException( "not implemented" );
	}

	@Override
	public KnossosUnsignedByteImageLoader fromXml( final Element elem, final File basePath, final AbstractSequenceDescription< ?, ?, ? > sequenceDescription )
	{
		final String configUrl = elem.getChildText( "configUrl" );
		final String urlFormat = elem.getChildText( "urlFormat" );
		return new KnossosUnsignedByteImageLoader( configUrl, urlFormat );
	}
}
