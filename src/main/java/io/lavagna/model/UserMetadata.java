package io.lavagna.model;

import lombok.Getter;

@Getter
public class UserMetadata {

    private final boolean showArchivedItems;

    public UserMetadata(boolean showArchivedItems) {
        this.showArchivedItems = showArchivedItems;
    }
}
