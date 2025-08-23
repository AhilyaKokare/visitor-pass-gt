import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

// Define an interface for the Page object from the Spring Boot backend
export interface Page<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  first: boolean;
  size: number;
  number: number;
  numberOfElements: number;
  empty: boolean;
}

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pagination.component.html'
})
export class PaginationComponent implements OnChanges {
  // Input: The Page object from the backend
  @Input() page: Page<any> | null = null;
  // Output: Event emitted when a new page is selected
  @Output() pageChange = new EventEmitter<number>();

  pages: number[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['page'] && this.page) {
      this.pages = this.generatePageNumbers(this.page.number, this.page.totalPages);
    }
  }

  goToPage(pageNumber: number): void {
    if (pageNumber >= 0 && pageNumber < (this.page?.totalPages ?? 0)) {
      this.pageChange.emit(pageNumber);
    }
  }

  private generatePageNumbers(currentPage: number, totalPages: number): number[] {
    const pageNumbers: number[] = [];
    // Logic to show a limited number of page links (e.g., 5 pages around the current one)
    let startPage: number, endPage: number;
    if (totalPages <= 5) {
      startPage = 1;
      endPage = totalPages;
    } else {
      if (currentPage <= 2) {
        startPage = 1;
        endPage = 5;
      } else if (currentPage + 2 >= totalPages) {
        startPage = totalPages - 4;
        endPage = totalPages;
      } else {
        startPage = currentPage;
        endPage = currentPage + 4;
      }
    }

    for (let i = startPage; i <= endPage; i++) {
      pageNumbers.push(i);
    }
    return pageNumbers;
  }
}
