package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.yandex.practicum.filmorate.annotation.ReleaseDateValidation;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    private Long id;
    @NotBlank
    private String name;

    @Size(max = 200)
    private String description;

    @NotNull
    @ReleaseDateValidation(startDate = "1895.12.28", message = "Укажите дату не ранее 1895.12.28")
    private LocalDate releaseDate;

    @NotNull
    @Positive
    private int duration;

    @Setter
    @Builder.Default
    private Set<Long> likes = new HashSet<>();

    @Setter
    @Builder.Default
    private Set<Genre> genres = new LinkedHashSet<>();

    @NotNull
    private Mpa mpa;
}
