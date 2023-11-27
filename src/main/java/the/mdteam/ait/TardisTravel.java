package the.mdteam.ait;

import mdteam.ait.AITMod;
import mdteam.ait.core.AITBlocks;
import mdteam.ait.core.blockentities.ExteriorBlockEntity;
import mdteam.ait.data.AbsoluteBlockPos;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TardisTravel {

    private State state = State.LANDED;
    private AbsoluteBlockPos.Directed position;
    private AbsoluteBlockPos.Directed destination;

    @Exclude
    protected final Tardis tardis;

    public TardisTravel(Tardis tardis, AbsoluteBlockPos.Directed pos) {
        this.tardis = tardis;
        this.position = pos;
    }

    public void setPosition(AbsoluteBlockPos.Directed pos) {
        this.position = pos;
    }

    public AbsoluteBlockPos.Directed getPosition() {
        return position;
    }

    public void materialise() {

    }

    public void dematerialise(boolean withRemat) {

    }

    public void setDestination(AbsoluteBlockPos.Directed pos, boolean withChecks) {
        this.destination = pos;
    }

    public AbsoluteBlockPos.Directed getDestination() {
        return destination;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void toggleHandbrake() {
        this.state.next(new TravelContext(this, this.position, this.destination));
    }

    public void placeExterior() {
        this.position.setBlockState(AITBlocks.EXTERIOR_BLOCK.getDefaultState());

        ExteriorBlockEntity exterior = new ExteriorBlockEntity(
                this.position, this.position.getBlockState()
        );

        exterior.setTardis(this.tardis);
        this.position.addBlockEntity(exterior);
    }

    public void deleteExterior() {

    }

    public enum State {
        LANDED(true) {
            @Override
            public void onEnable() {
                AITMod.LOGGER.info("ON: LANDED");
            }

            @Override
            public void onDisable() {
                AITMod.LOGGER.info("OFF: LANDED");
            }

            @Override
            public void schedule(TravelContext context) {

            }

            @Override
            public State getNext() {
                return DEMAT;
            }
        },
        DEMAT {
            @Override
            public void onEnable() {
                AITMod.LOGGER.info("ON: DEMAT");
            }

            @Override
            public void onDisable() {
                AITMod.LOGGER.info("OFF: DEMAT");
            }

            @Override
            public State getNext() {
                return FLIGHT;
            }
        },
        FLIGHT(true) {
            @Override
            public void onEnable() {
                AITMod.LOGGER.info("ON: FLIGHT");
            }

            @Override
            public void onDisable() {
                AITMod.LOGGER.info("OFF: LANDED");
            }

            @Override
            public void schedule(TravelContext context) {

            }

            @Override
            public State getNext() {
                return MAT;
            }
        },
        MAT {
            @Override
            public void onEnable() {
                AITMod.LOGGER.info("ON: MAT");
            }

            @Override
            public void onDisable() {
                AITMod.LOGGER.info("OFF: LANDED");
            }

            @Override
            public State getNext() {
                return LANDED;
            }
        };

        private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        private final boolean isStatic;

        State(boolean isStatic) {
            this.isStatic = isStatic;
        }

        State() {
            this(false);
        }

        public boolean isStatic() {
            return isStatic;
        }

        public ScheduledExecutorService getService() {
            return service;
        }

        public abstract void onEnable();
        public abstract void onDisable();
        public abstract State getNext();

        public void next(TravelContext context) {
            this.service.shutdown();
            this.onDisable();

            State next = this.getNext();
            next.schedule(context);

            next.onEnable();
            context.travel().setState(next);
        }

        public void schedule(TravelContext context) {
            this.getService().schedule(() -> {
                this.next(context);
            }, 2, TimeUnit.SECONDS);
        }
    }
}