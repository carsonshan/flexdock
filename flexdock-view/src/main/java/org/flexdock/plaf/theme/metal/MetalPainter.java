/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.flexdock.plaf.theme.metal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.flexdock.plaf.resources.paint.DefaultPainter;

/**
 * @author Claudio Romano
 */
public class MetalPainter extends DefaultPainter {

    MetalBumps activeBumps = new MetalBumps( 0, 0,
            MetalLookAndFeel.getPrimaryControlHighlight(),
            MetalLookAndFeel.getPrimaryControlDarkShadow(),
            MetalLookAndFeel.getPrimaryControl() );

    MetalBumps inactiveBumps = new MetalBumps( 0, 0,
            MetalLookAndFeel.getControlHighlight(),
            MetalLookAndFeel.getControlDarkShadow(),
            MetalLookAndFeel.getControl() );

    @Override
    public void paint(Graphics g, int width, int height, boolean active, JComponent titlebar) {
        MetalBumps bumbs = active ? activeBumps : inactiveBumps;
        bumbs.setBumpArea( width, height);
        bumbs.paintIcon(titlebar, g, 0, 0);
    }
}


/**
 *
 * @see javax.swing.plaf.metal.MetalBumbs
 */
class MetalBumps implements Icon {

    protected int xBumps;
    protected int yBumps;
    protected Color topColor;
    protected Color shadowColor;
    protected Color backColor;

    protected static Vector buffers = new Vector();
    protected BumpBuffer buffer;

    public MetalBumps( Dimension bumpArea ) {
        this( bumpArea.width, bumpArea.height );
    }

    public MetalBumps( int width, int height ) {
        this(width, height, MetalLookAndFeel.getPrimaryControlHighlight(),
             MetalLookAndFeel.getPrimaryControlDarkShadow(),
             MetalLookAndFeel.getPrimaryControlShadow());
    }

    public MetalBumps( int width, int height,
                       Color newTopColor, Color newShadowColor, Color newBackColor ) {
        setBumpArea( width, height );
        setBumpColors( newTopColor, newShadowColor, newBackColor );
    }

    private BumpBuffer getBuffer(GraphicsConfiguration gc, Color aTopColor,
                                 Color aShadowColor, Color aBackColor) {
        if (buffer != null && buffer.hasSameConfiguration(
                    gc, aTopColor, aShadowColor, aBackColor)) {
            return buffer;
        }
        BumpBuffer result = null;

        Enumeration elements = buffers.elements();

        while ( elements.hasMoreElements() ) {
            BumpBuffer aBuffer = (BumpBuffer)elements.nextElement();
            if ( aBuffer.hasSameConfiguration(gc, aTopColor, aShadowColor,
                                              aBackColor)) {
                result = aBuffer;
                break;
            }
        }
        if (result == null) {
            result = new BumpBuffer(gc, topColor, shadowColor, backColor);
            buffers.addElement(result);
        }
        return result;
    }

    public void setBumpArea( Dimension bumpArea ) {
        setBumpArea( bumpArea.width, bumpArea.height );
    }

    public void setBumpArea( int width, int height ) {
        xBumps = width / 2;
        yBumps = height / 2;
    }

    public void setBumpColors( Color newTopColor, Color newShadowColor, Color newBackColor ) {
        topColor = newTopColor;
        shadowColor = newShadowColor;
        backColor = newBackColor;
    }

    @Override
    public void paintIcon( Component c, Graphics g, int x, int y ) {
        GraphicsConfiguration gc = (g instanceof Graphics2D) ?
                ((Graphics2D)g).
                        getDeviceConfiguration() : null;

        buffer = getBuffer(gc, topColor, shadowColor, backColor);

        int bufferWidth = buffer.getImageSize().width;
        int bufferHeight = buffer.getImageSize().height;
        int iconWidth = getIconWidth();
        int iconHeight = getIconHeight();
        int x2 = x + iconWidth;
        int y2 = y + iconHeight;
        int savex = x;

        while (y < y2) {
            int h = Math.min(y2 - y, bufferHeight);
            for (x = savex; x < x2; x += bufferWidth) {
                int w = Math.min(x2 - x, bufferWidth);
                g.drawImage(buffer.getImage(),
                            x, y, x+w, y+h,
                            0, 0, w, h,
                            null);
            }
            y += bufferHeight;
        }
    }

    @Override
    public int getIconWidth() {
        return xBumps * 2;
    }

    @Override
    public int getIconHeight() {
        return yBumps * 2;
    }
}


class BumpBuffer {

    static final int IMAGE_SIZE = 64;
    static Dimension imageSize = new Dimension( IMAGE_SIZE, IMAGE_SIZE );

    transient Image image;
    Color topColor;
    Color shadowColor;
    Color backColor;
    private GraphicsConfiguration gc;

    public BumpBuffer(GraphicsConfiguration gc, Color aTopColor,
                      Color aShadowColor, Color aBackColor) {
        this.gc = gc;
        topColor = aTopColor;
        shadowColor = aShadowColor;
        backColor = aBackColor;
        createImage();
        fillBumpBuffer();
    }

    public boolean hasSameConfiguration(GraphicsConfiguration gc,
                                        Color aTopColor, Color aShadowColor,
                                        Color aBackColor) {
        if (this.gc != null) {
            if (!this.gc.equals(gc)) {
                return false;
            }
        } else if (gc != null) {
            return false;
        }
        return topColor.equals( aTopColor )       &&
               shadowColor.equals( aShadowColor ) &&
               backColor.equals( aBackColor );
    }

    /**
     * Returns the Image containing the bumps appropriate for the passed in
     * <code>GraphicsConfiguration</code>.
     */
    public Image getImage() {
        return image;
    }

    public Dimension getImageSize() {
        return imageSize;
    }

    /**
     * Paints the bumps into the current image.
     */
    private void fillBumpBuffer() {
        Graphics g = image.getGraphics();

        g.setColor( backColor );
        g.fillRect( 0, 0, IMAGE_SIZE, IMAGE_SIZE );

        g.setColor(topColor);
        for (int x = 0; x < IMAGE_SIZE; x+=4) {
            for (int y = 0; y < IMAGE_SIZE; y+=4) {
                g.drawLine( x, y, x, y );
                g.drawLine( x+2, y+2, x+2, y+2);
            }
        }

        g.setColor(shadowColor);
        for (int x = 0; x < IMAGE_SIZE; x+=4) {
            for (int y = 0; y < IMAGE_SIZE; y+=4) {
                g.drawLine( x+1, y+1, x+1, y+1 );
                g.drawLine( x+3, y+3, x+3, y+3);
            }
        }
        g.dispose();
    }

    /**
     * Creates the image appropriate for the passed in
     * <code>GraphicsConfiguration</code>, which may be null.
     */
    private void createImage() {
        if (gc != null) {
            image = gc.createCompatibleImage(IMAGE_SIZE, IMAGE_SIZE);
        } else {
            int cmap[] = { backColor.getRGB(),
                topColor.getRGB(),
                shadowColor.getRGB()
            };
            IndexColorModel icm = new IndexColorModel(8, 3, cmap, 0, false, -1,
                    DataBuffer.TYPE_BYTE);
            image = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE,
                                      BufferedImage.TYPE_BYTE_INDEXED, icm);
        }
    }
}

