package com.aitusoftware.example.aeron.service;

import io.aeron.AvailableImageHandler;
import io.aeron.Image;
import io.aeron.UnavailableImageHandler;

public final class ImageAvailabilityHandler implements AvailableImageHandler, UnavailableImageHandler
{
    @Override
    public void onAvailableImage(final Image image)
    {
        System.out.printf("Image available: %s%n", image.subscription().channel());
    }

    @Override
    public void onUnavailableImage(final Image image)
    {
        System.out.printf("Image unavailable: %s%n", image.subscription().channel());
    }
}
