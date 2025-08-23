/**
 * A generic interface that matches the structure of a Page object
 * returned by Spring Data JPA for paginated responses.
 *
 * @template T The type of the content in the page.
 */
export interface Page<T> {
  content: T[]; // The actual list of items for the current page
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number; // The current page number (0-indexed)
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}