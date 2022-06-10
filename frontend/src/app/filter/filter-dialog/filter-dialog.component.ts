import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Filter } from 'src/app/filter/dto/filter';
import { FilterDetails } from 'src/app/filter/dto/filter-details';
import { FilterRequest } from 'src/app/filter/dto/filter-request';
import { AddEvent } from 'src/app/common/utils/add-event';
import { FilterService } from 'src/app/filter/filter.service';
import { TreeService } from 'src/app/utils/tree.service';

export interface FilterForm {
  name: FormControl<string>;
  searchString: FormControl<string>;
  color: FormControl<string>;
}

@Component({
  selector: 'app-filter-dialog',
  templateUrl: './filter-dialog.component.html',
  styleUrls: ['./filter-dialog.component.css']
})
export class FilterDialogComponent implements OnInit {
  addFilterForm: FormGroup<FilterForm>;

  @Output() closeEvent = new EventEmitter<boolean>();
  @Input() data: AddEvent<FilterDetails> | undefined;
  filter: FilterDetails | undefined;
  after: boolean = false;
  before: boolean = false;
  editMode: boolean = false;

  favorite: boolean = false;

  constructor(private fb: FormBuilder, public tree : TreeService, 
    private filterService: FilterService) { 
    this.addFilterForm = this.fb.nonNullable.group({
      name: ['', Validators.required],
      searchString: ['', Validators.required],
      color: ['#888', Validators.required]
    });
  }

  ngOnInit(): void {
    if(this.data) {
      this.filter = this.data.object;
      this.after = this.data.after;
      this.before = this.data.before;
    }
    
    if(this.filter && !this.after && !this.before) {
      this.editMode = true;
    }
    if(this.editMode && this.filter) {
      this.addFilterForm.setValue({
        name: this.filter.name,
        searchString: this.filter.searchString,
        color: this.filter.color
      });
    }
  }

  colors: string[] = ['#ADA', '#EDA', '#DDD', '#888', '#B8255F', '#25B87D',
  '#B83325', '#B825A9', '#FF9933', '#3399FF', '#FFFF33', '#FF3333', '#7ECC49',
  '#49CC56', '#BFCC49', '#9849CC', '#158FAD', '#AD3315', '#1543AD', '#15AD80'];
  showColors = false;

  toggleShowColors(): void {
    this.showColors = !this.showColors;
  }

  get color(): string {
    return this.addFilterForm.controls.color.value;
  }

  chooseColor(color: string): void {
    this.addFilterForm.setValue({
      name: this.addFilterForm.controls.name.value,
      searchString: this.addFilterForm.controls.searchString.value,
      color: color
    });
  }

  closeFilterModal(): void {
    this.closeEvent.emit(true);
  }

  switchFav(): void {
    this.favorite = !this.favorite;
  }

  addFilterModal(): void {
    let request: FilterRequest = {
      name: this.addFilterForm.controls.name.value,
      color: this.addFilterForm.controls.color.value,
      searchString: this.addFilterForm.controls.searchString.value,
      favorite: this.favorite
    };
    
    this.closeFilterModal();
    
    if(this.filter && this.after) {
      this.addAfterFilterModal(request, this.filter);
    } else if(this.filter && this.before) {
      this.addBeforeFilterModal(request, this.filter);
    } else if(this.filter) {
      this.editFilterModal(request, this.filter.id);
    } else {
      this.addEndFilterModal(request)
    }
  }

  addEndFilterModal(request: FilterRequest): void {
    this.filterService.addFilter(request).subscribe(
      (response: Filter) => {
        this.tree.addNewFilter(response);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  addBeforeFilterModal(request: FilterRequest, filterBefore: FilterDetails): void {
    this.filterService.addFilterBefore(request, filterBefore.id).subscribe(
      (response: Filter, beforeId: number = filterBefore.id) => {
        this.tree.addNewFilterBefore(response, beforeId);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  editFilterModal(request: FilterRequest, id: number): void {
    this.filterService.updateFilter(id, request).subscribe(
      (response: Filter) => {
        this.tree.updateFilter(response, id);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  addAfterFilterModal(request: FilterRequest, filterAfter: FilterDetails): void {
    this.filterService.addFilterAfter(request, filterAfter.id).subscribe(
      (response: Filter, afterId: number = filterAfter.id) => {
        this.tree.addNewFilterAfter(response, afterId);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  @HostListener("window:keydown.escape", ["$event"])
  handleKeyboardEscapeEvent(): void {
    this.closeFilterModal();
  }

  @HostListener("window:keydown.enter", ["$event"])
  handleKeyboardEnterEvent(): void {
    if(this.addFilterForm?.valid) {
      this.addFilterModal();
    }
  }
}
