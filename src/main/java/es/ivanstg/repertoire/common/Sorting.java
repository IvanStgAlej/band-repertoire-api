package es.ivanstg.repertoire.common;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Restricts {@code ?sort=} to plain columns. Sorting by a collection (e.g. a rehearsal's pieces) would make
 * the query join that collection and return duplicated rows, and an unknown property would fail deep in the
 * query; both are rejected up front with 400 and the list of valid properties.
 */
public final class Sorting {

    private Sorting() {
    }

    public static Pageable requireSortable(Pageable pageable, String... allowed) {
        List<String> properties = List.of(allowed);
        for (Sort.Order order : pageable.getSort()) {
            if (!properties.contains(order.getProperty())) {
                throw new BadRequestException("Cannot sort by '" + order.getProperty()
                        + "'. Sortable properties: " + String.join(", ", properties));
            }
        }
        return pageable;
    }
}
