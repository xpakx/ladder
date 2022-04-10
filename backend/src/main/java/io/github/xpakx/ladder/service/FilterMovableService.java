package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.aspect.NotifyOnFilterChange;
import io.github.xpakx.ladder.entity.Filter;
import io.github.xpakx.ladder.entity.dto.FilterRequest;
import io.github.xpakx.ladder.entity.dto.IdRequest;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.FilterRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class FilterMovableService {
    private final FilterRepository filterRepository;
    private final UserAccountRepository userRepository;

    /**
     * Move filter at first position
     * @param userId ID of an owner of filters
     * @param filterToMoveId ID of the filter to move
     * @return Moved filter
     */
    @NotifyOnFilterChange
    public Filter moveFilterAsFirst(Integer userId, Integer filterToMoveId) {
        Filter filterToMove = filterRepository.findByIdAndOwnerId(filterToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent filter!"));
        filterRepository.incrementGeneralOrderByOwnerId(
                userId,
                LocalDateTime.now()
        );
        filterToMove.setGeneralOrder(1);
        filterToMove.setModifiedAt(LocalDateTime.now());
        return filterRepository.save(filterToMove);
    }

    /**
     * Add new filter with order after given filter
     * @param request Request with data to build new filter
     * @param userId ID of an owner of filters
     * @param filterId ID of the filter which should be before newly created filter
     * @return Newly created filter
     */
    @NotifyOnFilterChange
    public Filter addFilterAfter(FilterRequest request, Integer userId, Integer filterId) {
        Filter filterToAdd = buildFilterToAddFromRequest(request, userId);
        Filter filter = filterRepository.findByIdAndOwnerId(filterId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing after non-existent filter!"));
        filterToAdd.setGeneralOrder(filter.getGeneralOrder()+1);
        filterToAdd.setModifiedAt(LocalDateTime.now());
        filterRepository.incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThan(
                userId,
                filter.getGeneralOrder(),
                LocalDateTime.now()
        );
        return filterRepository.save(filterToAdd);
    }

    /**
     * Add new filter with order before given filter
     * @param request Request with data to build new filter
     * @param userId ID of an owner of filters
     * @param filterId ID of the filter which should be after newly created filter
     * @return Newly created filter
     */
    @NotifyOnFilterChange
    public Filter addFilterBefore(FilterRequest request, Integer userId, Integer filterId) {
        Filter filterToAdd = buildFilterToAddFromRequest(request, userId);
        Filter filter = filterRepository.findByIdAndOwnerId(filterId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing before non-existent filter!"));
        filterToAdd.setGeneralOrder(filter.getGeneralOrder());
        filterToAdd.setModifiedAt(LocalDateTime.now());
        filterRepository.incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThanEqual(
                userId,
                filter.getGeneralOrder(),
                LocalDateTime.now()
        );
        return filterRepository.save(filterToAdd);
    }

    /**
     * Move filter after given filter
     * @param request Request with id of the filter which should be before moved filter
     * @param userId ID of an owner of filters
     * @param filterToMoveId ID of the filter to move
     * @return Moved filter
     */
    public Filter moveFilterAfter(IdRequest request, Integer userId, Integer filterToMoveId) {
        Filter filterToMove = filterRepository.findByIdAndOwnerId(filterToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent filter!"));
        Filter afterFilter = findIdFromIdRequest(request);
        filterRepository.incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThan(
                userId,
                afterFilter.getGeneralOrder(),
                LocalDateTime.now()
        );
        filterToMove.setGeneralOrder(afterFilter.getGeneralOrder() + 1);
        filterToMove.setModifiedAt(LocalDateTime.now());
        return filterRepository.save(filterToMove);
    }

    private Filter findIdFromIdRequest(IdRequest request) {
        return hasId(request) ? filterRepository.findById(request.getId()).orElse(null) : null;
    }

    private boolean hasId(IdRequest request) {
        return request.getId() != null;
    }

    private Filter buildFilterToAddFromRequest(FilterRequest request, Integer userId) {
        return Filter.builder()
                .name(request.getName())
                .owner(userRepository.getById(userId))
                .color(request.getColor())
                .favorite(request.isFavorite())
                .searchString(request.getSearchString())
                .modifiedAt(LocalDateTime.now())
                .build();
    }
}
