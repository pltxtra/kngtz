package com.holidaystudios.kngt.controller;

import com.holidaystudios.kngt.Defs;
import com.holidaystudios.kngt.Direction;
import com.holidaystudios.kngt.TileTypes;
import com.holidaystudios.kngt.model.KnightModel;
import com.holidaystudios.kngt.view.ViewListener;
import com.holidaystudios.kngt.view.actors.KnightView;

/**
 * Created by tedbjorling on 2014-02-26.
 */
public class Knight implements ViewListener {

    private KnightModel model;
    private KnightView view;
    private Game game;
    private Direction stickyDirection = null;

    public Knight(final Game _game) {
        game = _game;
        model = new KnightModel();
        view = new KnightView();
        view.addListener(this);
    }

    public KnightModel getModel() {
        return model;
    }

    public KnightView getView() {
        return view;
    }

    public void move(final Direction d, final Boolean sticky) {

        if (stickyDirection != null && d != stickyDirection) {
            //Override next direction with new one
            stickyDirection = d;
            return;
        }

        //What is the target tile?
        Integer posX = getModel().getPosX();
        Integer posY = getModel().getPosY();
        switch (d) {
            case east: posX++; break;
            case west: posX--; break;
            case north: posY--; break;
            case south: posY++; break;
        }

        final Integer targetTile = game.getModel().getRoomBitmap(getModel().getRoomX(), getModel().getRoomY())[posY][posX];
        if (targetTile == TileTypes.TILE_FLOOR) {
            model.setPosX(posX);
            model.setPosY(posY);

            if (sticky) {
                stickyDirection = d;
            } else {
                stickyDirection = null;
            }
            view.move(d);

        } else if (targetTile == TileTypes.TILE_DOOR) {
            gotoNextRoom(d);

            //XXX handle this through sending an event instead, signaling this properly to others
            game.getView().renderRoom(game.getModel().getRoomBitmap(getModel().getRoomX(), getModel().getRoomY()));
        }

    }

    public void stopMoving(final Direction d) {
        //Make sure we are not interrupting the current sticky direction if an "old" key was released
        if (d == stickyDirection)
            stickyDirection = null;
    }

    public void gotoNextRoom(final Direction d) {
        switch (d) {
            case north:
                setRoom(getModel().getRoomX(), getModel().getRoomY()-1);
                setPosition(getModel().getPosX(), Defs.TILES_PER_DISTANCE - 2);
                break;
            case south:
                setRoom(getModel().getRoomX(), getModel().getRoomY()+1);
                setPosition(getModel().getPosX(), 1);
                break;
            case east:
                setRoom(getModel().getRoomX() + 1, getModel().getRoomY());
                setPosition(1, getModel().getPosY());
                break;
            case west:
                setRoom(getModel().getRoomX() - 1, getModel().getRoomY());
                setPosition(Defs.TILES_PER_DISTANCE - 2, getModel().getPosY());
                break;
        }
    }

    public void setRoom(final Integer roomX, final Integer roomY) {
        this.model.setRoomX(roomX);
        this.model.setRoomY(roomY);
    }

    public void setPosition(final Integer posX, final Integer posY) {
        this.model.setPosX(posX);
        this.model.setPosY(posY);
        this.view.setPosition(posX * Defs.TILE_SIZE, (Defs.TILE_SIZE * (Defs.TILES_PER_DISTANCE-1- posY))); //Inverse Y-axis
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Room : " + getModel().getRoomX() + ", " + getModel().getRoomY());
        sb.append(System.lineSeparator());
        sb.append("Pos  : " + getModel().getPosX() + ", " + getModel().getPosY());
        return sb.toString();
    }


    @Override
    public void handleViewEvent(EventType type, Object data) {
        switch (type) {
            case doneMoving:
                if (stickyDirection != null) {
                    move(stickyDirection, true);
                }
                break;
        }
    }
}
