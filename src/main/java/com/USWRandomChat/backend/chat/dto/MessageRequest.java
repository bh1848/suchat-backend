package com.USWRandomChat.backend.chat.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest implements Serializable {

    private static final long serialVersionUID = 1935108422412743210L;

    @NotNull
    private String roomId;

    @NotNull
    private String sender;

    @NotNull
    private String contents;

    public void setContents(String contents) {
        this.contents = contents;
    }
}
