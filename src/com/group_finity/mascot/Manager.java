package com.group_finity.mascot;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import java.awt.Point;

/**
 * Maintains and manages list of mascots.
 */
public class Manager {

	private static final Logger log = Logger.getLogger(Manager.class.getName());

	/**Interval of timer in milliseconds. set to 25fps*/
	public static final int TICK_INTERVAL = 40;

	/** A list of actual mascots*/
	private final List<Mascot> mascots = new ArrayList<>();

	/**
	* The mascot will be added later.
	* ConcurrentModificationException to prevent the addition of the mascot while
	 * {@link #tick()} are each simultaneously reflecting.
	 */
	private final Set<Mascot> added = new LinkedHashSet<>();

	/**
	* The mascot will be removed later.
	* {@link java.util.ConcurrentModificationException} to prevent the deletion of the mascot {@link #tick()}  are
	 * each simultaneously reflecting.
	 */
	private final Set<Mascot> removed = new LinkedHashSet<>();

	/** timing thread that controls the ticks */
	private Thread thread;


	/** causes the program to exit on the removal of the last mascot if set to true */
	private boolean exitOnLastRemoved = true;

	public Manager() {

		new Thread() {
			{
				this.setDaemon(true);
				this.start();
			}

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(Integer.MAX_VALUE);
					} catch (final InterruptedException ignored) {}
				}
			}

		};
	}
	
	public void start() {
		if (thread!=null && thread.isAlive()) {
			return;
		}
		
		thread = new Thread(() -> {
			long prev = System.nanoTime() / 1000000;

			try {

				while (true) {
					//inner
					while (true) {
						final long cur = System.nanoTime() / 1000000;
						if (cur - prev >= TICK_INTERVAL) {

							if (cur > prev + TICK_INTERVAL * 2) { //checks for a skip
								prev = cur;
							} else {
								prev += TICK_INTERVAL;
							}
							break; //breaks inner while
						}
						Thread.sleep(1, 0);
					}

					tick();
				}

			} catch (final InterruptedException ignored) {}

		});

		thread.setDaemon(false);
		thread.start();
	}
	
	public void stop() {
		if ( thread==null || !thread.isAlive() ) {
			return;
		}
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException ignored) {}
	}

	private void tick() {

		// Update the environmental information
		NativeFactory.getInstance().getEnvironment().tick();

		synchronized (this.getMascots()) {

			// Add the mascot if it should be added
			for (final Mascot mascot : this.getAdded()) {
				this.getMascots().add(mascot);
			}
			this.getAdded().clear();

			// Remove the mascot if it should be removed
			for (final Mascot mascot : this.getRemoved()) {
				this.getMascots().remove(mascot);
			}
			this.getRemoved().clear();

			// Advance mascot's time
			for (final Mascot mascot : this.getMascots()) {
				mascot.tick();
			}
			
			// Draw the mascot
			for (final Mascot mascot : this.getMascots()) {
				mascot.apply();
			}
		}

		if (isExitOnLastRemoved()) {
			if (this.getMascots().size() == 0) {
				Main.getInstance().exit();
			}
		}
	}

	public void add(final Mascot mascot) {
		synchronized (this.getAdded()) {
			this.getAdded().add(mascot);
			this.getRemoved().remove(mascot);
		}
		mascot.setManager(this);
	}

	public void remove(final Mascot mascot) {
		synchronized (this.getAdded()) {
			this.getAdded().remove(mascot);
			this.getRemoved().add(mascot);
		}
		mascot.setManager(null);
	}

	public void setBehaviorAll(final String name) {
		synchronized (this.getMascots()) {
			for (final Mascot mascot : this.getMascots()) {
				try {
					Configuration conf = Main.getInstance().getConfiguration(mascot.getImageSet());
				    mascot.setBehavior( conf.buildBehavior(conf.getSchema().getString(name)));
				} catch (final BehaviorInstantiationException e) {
					log.log(Level.SEVERE, "Failed to initialize the following actions", e);
					Main.showError(Main.getInstance().getLanguageBundle()
							.getString("FailedSetBehaviourErrorMessage" ) + "\n" + e.getMessage()
							+ "\n" + Main.getInstance().getLanguageBundle().getString( "SeeLogForDetails" ) );
					mascot.dispose();
				} catch (final CantBeAliveException e) {
					log.log(Level.SEVERE, "Fatal Error", e);
                                        Main.showError( Main.getInstance().getLanguageBundle()
												.getString( "FailedSetBehaviourErrorMessage" ) + "\n"
												+ e.getMessage()
												+ "\n" + Main.getInstance( ).getLanguageBundle( ).getString( "SeeLogForDetails" ));
					mascot.dispose();
				}
			}
		}
	}	
	
	public void setBehaviorAll(final Configuration configuration, final String name, String imageSet) {
		synchronized (this.getMascots()) {
			for (final Mascot mascot : this.getMascots()) {
				try {
					if( mascot.getImageSet().equals(imageSet) ) {
						mascot.setBehavior(configuration.buildBehavior( configuration.getSchema( ).getString( name ) ) );						
					}
				} catch (final BehaviorInstantiationException e) {
					log.log(Level.SEVERE, "Failed to initialize the following actions", e);
					Main.showError( Main.getInstance( ).getLanguageBundle( ).getString( "FailedSetBehaviourErrorMessage" ) + "\n" + e.getMessage( ) + "\n" + Main.getInstance( ).getLanguageBundle( ).getString( "SeeLogForDetails" ) );
					mascot.dispose();
				} catch (final CantBeAliveException e) {
					log.log(Level.SEVERE, "Fatal Error", e);
					Main.showError( Main.getInstance( ).getLanguageBundle( ).getString( "FailedSetBehaviourErrorMessage" ) + "\n" + e.getMessage( ) + "\n" + Main.getInstance( ).getLanguageBundle( ).getString( "SeeLogForDetails" ) );
					mascot.dispose();
				}
			}
		}
	}

	public void remainOne() {
		synchronized (this.getMascots()) {
			int totalMascots = this.getMascots().size();
			for (int i = totalMascots - 1; i > 0; --i) {
				this.getMascots().get(i).dispose();				
			}
		}
	}
	
	public void remainOne(String imageSet) {
		synchronized (this.getMascots()) {
			int totalMascots = this.getMascots().size();
			boolean isFirst = true;
			for (int i = totalMascots - 1; i >= 0; --i) {
				Mascot m = this.getMascots().get(i);
				if (m.getImageSet().equals(imageSet) && isFirst) {
					isFirst = false;
				} else if( m.getImageSet().equals(imageSet) && !isFirst) {
					m.dispose();
				}
			}
		}
	}

	/** Disposes all mascots made from the specified imageSet*/
	public void remainNone(String imageSet) {
		synchronized (this.getMascots()) {
			int totalMascots = this.getMascots().size();
			for (int i = totalMascots - 1; i >= 0; --i) {
				Mascot m = this.getMascots().get(i);
				if (m.getImageSet().equals(imageSet)) {
					m.dispose();
				}
			}
		}
	}


    public int getCount() {
        return getCount(null);
    }
    
    public int getCount(String imageSet) {

        synchronized(getMascots()) {

            if( imageSet == null ) {
                return getMascots().size( );
            }

            else {
                int count = 0;

                for(int index = 0; index < getMascots().size(); index++) {

                    Mascot m = getMascots( ).get( index );
                    if( m.getImageSet( ).equals( imageSet ) )
                        count++;
                }
                return count;
            }
        }
    }
        
	/**
	 * Returns another Mascot with the given affordance.
	 * @param affordance the affordance being searched for
	 * @return A reference to a mascot with the required affordance, or null if a match is not found
	 */
	public WeakReference<Mascot> getMascotWithAffordance( String affordance )
	{
		synchronized(this.getMascots()) {
			for (final Mascot mascot : this.getMascots()) {

				if (mascot.getAffordances().contains(affordance)) {
					return new WeakReference<Mascot>( mascot );
				}

			}
		}

		return null;
	}

    public boolean hasOverlappingMascotsAtPoint(Point anchor) {

        int count = 0;
        
        synchronized (this.getMascots()) {

            for (final Mascot mascot : this.getMascots()) {

                if (mascot.getAnchor().equals(anchor)) {
					count++;
				}
                if (count > 1) {
					return true;
				}
            }
        }

        return false;
    }

	public void disposeAll() {
		synchronized (this.getMascots()) {
			for (int i = this.getMascots().size() - 1; i >= 0; --i) {
				this.getMascots().get(i).dispose();
			}
		}
	}


	//------gs-------//

	public void setExitOnLastRemoved(boolean exitOnLastRemoved) {
		this.exitOnLastRemoved = exitOnLastRemoved;
	}
	public boolean isExitOnLastRemoved() {
		return exitOnLastRemoved;
	}

	private List<Mascot> getMascots() {
		return this.mascots;
	}

	private Set<Mascot> getAdded() {
		return this.added;
	}

	private Set<Mascot> getRemoved() {
		return this.removed;
	}
}
