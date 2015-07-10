package bdv.bigcat;

import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.InvertibleRealTransform;
import net.imglib2.realtransform.RealViews;
import net.imglib2.view.Views;
import bdv.img.cache.CacheHints;
import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.render.DefaultMipmapOrdering;
import bdv.viewer.render.MipmapOrdering;
import bdv.viewer.render.SetCacheHints;

public class WaveySource< T > implements Source< T >, MipmapOrdering, SetCacheHints
{
	public static < T > SourceAndConverter< T > wrap( final SourceAndConverter< T > wrap, final String name )
	{
		return new SourceAndConverter< T >(
				new WaveySource< T >( wrap.getSpimSource(), name ),
				wrap.getConverter(),
				wrap.asVolatile() == null ? null : wrap( wrap.asVolatile(), name ) );
	}

	/**
	 * The wrapped {@link Source}.
	 */
	private final Source< T > source;

	private final String name;

	/**
	 * This is either the {@link #source} itself, if it implements
	 * {@link MipmapOrdering}, or a {@link DefaultMipmapOrdering}.
	 */
	private final MipmapOrdering sourceMipmapOrdering;

	/**
	 * This is either the {@link #source} itself, if it implements
	 * {@link SetCacheHints}, or a {@link SetCacheHints} doing
	 * nothing.
	 */
	private final SetCacheHints sourceSetCacheHints;

	public WaveySource( final Source< T > source, final String name )
	{
		this.source = source;
		this.name = name;

		sourceMipmapOrdering = MipmapOrdering.class.isInstance( source ) ?
				( MipmapOrdering ) source : new DefaultMipmapOrdering( source );

		sourceSetCacheHints = SetCacheHints.class.isInstance( source ) ?
				( SetCacheHints ) source : SetCacheHints.empty;
	}

	@Override
	public boolean isPresent( final int t )
	{
		return source.isPresent( t );
	}

	@Override
	public RandomAccessibleInterval< T > getSource( final int t, final int level )
	{
		return Views.interval(
				Views.raster( getInterpolatedSource( t, level, Interpolation.NEARESTNEIGHBOR ) ),
				estimateBoundingInterval( t, level )
				);
	}

	private Interval estimateBoundingInterval( final int t, final int level )
	{
		final Interval wrappedInterval = source.getSource( t, level );
		// TODO: Do something meaningful: apply transform, estimate bounding box, etc.
		return wrappedInterval;
	}

	@Override
	public RealRandomAccessible< T > getInterpolatedSource( final int t, final int level, final Interpolation method )
	{
		final AffineTransform3D transform = new AffineTransform3D();
		source.getSourceTransform( t, level, transform );
		final RealRandomAccessible< T > sourceRealAccessible = RealViews.affineReal( source.getInterpolatedSource( t, level, method ), transform );


		return RealViews.transformReal( sourceRealAccessible, new InvertibleRealTransform()
		{
			@Override
			public int numSourceDimensions()
			{
				return 3;
			}

			@Override
			public int numTargetDimensions()
			{
				return 3;
			}

			@Override
			public void apply( final double[] source, final double[] target )
			{}

			@Override
			public void apply( final float[] source, final float[] target )
			{}

			@Override
			public void apply( final RealLocalizable source, final RealPositionable target )
			{}

			@Override
			public void applyInverse( final double[] source, final double[] target )
			{}

			@Override
			public void applyInverse( final float[] source, final float[] target )
			{}

			@Override
			public void applyInverse( final RealPositionable source, final RealLocalizable target )
			{
				for ( int d = 0; d < 3; ++d )
					source.setPosition( target.getDoublePosition( d ) + Math.sin( target.getDoublePosition( d ) / 10 ) * 5, d );
			}

			@Override
			public InvertibleRealTransform inverse()
			{
				return null;
			}

			@Override
			public InvertibleRealTransform copy()
			{
				return this;
			}
		} );
	}

	@Override
	public void getSourceTransform( final int t, final int level, final AffineTransform3D transform )
	{
		transform.identity();
	}

	@Override
	public AffineTransform3D getSourceTransform( final int t, final int level )
	{
		return new AffineTransform3D();
	}

	@Override
	public T getType()
	{
		return source.getType();
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public VoxelDimensions getVoxelDimensions()
	{
		return null;
	}

	@Override
	public int getNumMipmapLevels()
	{
		return source.getNumMipmapLevels();
	}

	@Override
	public void setCacheHints( final int level, final CacheHints cacheHints )
	{
		sourceSetCacheHints.setCacheHints( level, cacheHints );
	}

	@Override
	public synchronized MipmapHints getMipmapHints( final AffineTransform3D screenTransform, final int timepoint, final int previousTimepoint )
	{
		return sourceMipmapOrdering.getMipmapHints( screenTransform, timepoint, previousTimepoint );
	}
}
