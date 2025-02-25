package com.example.spaceinvader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

class Bullet {
    int x, y;
    Bitmap bitmap;

    Bullet(int x, int y, Bitmap bitmap) {
        this.x = x;
        this.y = y;
        this.bitmap = bitmap;
    }

    boolean move(int speed, int screenWidth) {
        x += speed;
        return (x < 0 || x > screenWidth);
    }

    void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y, null);
    }

    Rect getRect() {
        return new Rect(x, y, x + bitmap.getWidth(), y + bitmap.getHeight());
    }
}