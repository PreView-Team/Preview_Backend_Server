package com.preview.preview.module.form.application.dto;

import com.preview.preview.module.form.domain.Form;
import com.preview.preview.module.job.domain.Job;
import com.preview.preview.module.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
public class FormGetResponseDto {
    private long formId;
    private String mentoNickname;
    private LocalDateTime createTime;
    private String name;
    private String phoneNumber;
    private Set<Job> jobNames;
    private String content; // 상담 받고 싶은 내용
    private boolean status;

    public static FormGetResponseDto from(Form form, User user){
        if (form == null) return null;
        return FormGetResponseDto.builder()
                .formId(form.getId())
                .mentoNickname(form.getUser().getNickname())
                .createTime(form.getCreatedDate())
                .name(form.getName())
                .phoneNumber(form.getPhoneNumber())
                .jobNames(user.getLikedJobs())
                .content(form.getContent())
                .status(form.isStatus())
                .build();
    }
}
