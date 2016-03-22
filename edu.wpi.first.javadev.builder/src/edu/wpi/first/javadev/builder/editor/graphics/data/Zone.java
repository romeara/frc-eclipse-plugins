package edu.wpi.first.javadev.builder.editor.graphics.data;

/**
 * 
 * 
 * @author Joe Grinstead
 */
public enum Zone {

	CENTER (0, 0) {
		@Override
		public Zone[] split() {
			return new Zone[0];
		}

		@Override
		public Zone opposite() {
			return CENTER;
		}
		
		@Override
		public Zone[] adjacents() {
			return new Zone[] { CENTER, CENTER };
		}
	},
	EAST (1, 0) {
		@Override
		public Zone[] split() {
			return new Zone[] { EAST };
		}

		@Override
		public Zone opposite() {
			return WEST;
		}

		@Override
		public Zone[] adjacents() {
			return new Zone[] { NORTHEAST, SOUTHEAST };
		}
	},
	NORTHEAST (1, -1) {
		@Override
		public Zone[] split() {
			return new Zone[] { NORTH, EAST };
		}

		@Override
		public Zone opposite() {
			return SOUTHWEST;
		}
	},
	NORTH (0, -1) {
		@Override
		public Zone[] split() {
			return new Zone[] { NORTH };
		}

		@Override
		public Zone opposite() {
			return SOUTH;
		}

		@Override
		public Zone[] adjacents() {
			return new Zone[] { NORTHWEST, NORTHEAST };
		}
	},
	NORTHWEST (-1, -1) {
		@Override
		public Zone[] split() {
			return new Zone[] { WEST, NORTH };
		}

		@Override
		public Zone opposite() {
			return SOUTHEAST;
		}
	},
	WEST (-1, 0) {
		@Override
		public Zone[] split() {
			return new Zone[] { WEST };
		}

		@Override
		public Zone opposite() {
			return EAST;
		}

		@Override
		public Zone[] adjacents() {
			return new Zone[] { SOUTHWEST, NORTHWEST };
		}
	},
	SOUTHWEST (-1, 1) {
		@Override
		public Zone[] split() {
			return new Zone[] { SOUTH, WEST };
		}

		@Override
		public Zone opposite() {
			return NORTHEAST;
		}
	},
	SOUTH (0, 1) {
		@Override
		public Zone[] split() {
			return new Zone[] { SOUTH };
		}

		@Override
		public Zone opposite() {
			return NORTH;
		}

		@Override
		public Zone[] adjacents() {
			return new Zone[] { SOUTHEAST, SOUTHWEST };
		}
	},
	SOUTHEAST (1, 1) {
		@Override
		public Zone[] split() {
			return new Zone[] { EAST, SOUTH };
		}

		@Override
		public Zone opposite() {
			return NORTHWEST;
		}
	};

	public int	x;
	public int	y;

	private Zone(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int numberOfDirections() {
		return x == 0 ? y == 0 ? 0 : 1 : 2;
	}

	public Zone[] adjacents() {
		// This only works for composite directions (Northwest, Southwest, etc)
		// All others have to override this method
		return split();
	}
	
	public Zone intersection(Zone zone) {
		return intersection(this, zone);
	}

	public abstract Zone[] split();

	public abstract Zone opposite();

	public static Zone get(int x, int y) {
		x = sign(x);
		y = sign(y);
		for (Zone zone : Zone.values()) {
			if (zone.x == x && zone.y == y) return zone;
		}
		return null;
	}

	public static Zone intersection(Zone a, Zone b) {
		int x = a.x + b.x;
		int y = a.y + b.y;
		return get(x == 2 || x == -2 ? x : 0, y == 2 || y == -2 ? y : 0);
	}
	
	public static int sign(int n) {
		return n <= 0 ? n < 0 ? -1 : 0 : 1;
	}
}
