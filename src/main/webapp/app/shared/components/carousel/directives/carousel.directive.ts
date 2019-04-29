import {Directive, EventEmitter, Input, OnDestroy, OnInit, Output, TemplateRef, ViewContainerRef} from '@angular/core';
import {CarouselContext} from "../model/CarouselContext";

/**
 * Directive used to manage the rotation of our carousel
 */
@Directive({
  selector: '[carousel]'
})
export class CarouselDirective implements OnInit, OnDestroy {
  /**
   * Object used as interface between the directive and the component that called this directive
   */
  context: CarouselContext;
  /**
   * Current rotation index
   */
  index = 0;
  /**
   * Timer used to manage the automatic rotation
   */
  timer: number;

  /**
   * The list of items that we need to display
   */
  @Input('carouselFrom') carouselItems: any[];

  /**
   * Set the autoplay to on or off default is off
   */
  @Input('carouselAutoplay') autoplay: 'on' | 'off' = 'on';

  /**
   * Event raised when the current element has changed
   */
  @Output() onCurrentItemChange: EventEmitter<any> = new EventEmitter();

  /**
   * Constructor
   *
   * @param carouselContextTemplateRef  Object used to inject our context object
   * @param viewContainerRef            Map the global context view our context
   */
  constructor(private carouselContextTemplateRef: TemplateRef<CarouselContext>,
              private viewContainerRef: ViewContainerRef) {
  }

  /**
   * Init the carousel
   */
  ngOnInit(): void {
    this.context = {
      $implicit: this.carouselItems[0],
      index: 0,
      playing: this.autoplay === 'on',
      controller: {
        next: () => this.next(),
        prev: () => this.prev(),
        stop: () => this.stop(),
        play: () => this.play()
      }
    };

    this.autoplay === 'on' ? this.setAutoplayTimer() : this.clearAutoplayTimer();
    this.viewContainerRef.createEmbeddedView(this.carouselContextTemplateRef, this.context);
  }

  /**
   * Go to the next slide
   */
  next() {
    if (this.context.playing) {
      this.resetTimer();
    }

    this.index++;
    if (this.index >= this.carouselItems.length) {
      this.index = 0;
    }
    this.context.$implicit = this.carouselItems[this.index];
    this.context.index = this.index;
  }

  /**
   * Go back to the slide
   */
  prev() {
    if (this.context.playing) {
      this.resetTimer();
    }

    this.index--;
    if (this.index < 0) {
      this.index = this.carouselItems.length - 1;
    }
    this.context.$implicit = this.carouselItems[this.index];
    this.context.index = this.index;
  }

  /**
   * Stop the carousel
   */
  stop() {
    this.context.playing = false;
    this.clearAutoplayTimer();
  }

  /**
   * Play carousel
   */
  play() {
    this.context.playing = true;
    this.setAutoplayTimer();
  }

  /**
   * Reset the timer
   */
  private resetTimer() {
    this.clearAutoplayTimer();
    this.setAutoplayTimer();
  }

  /**
   * Clear the timer
   */
  private clearAutoplayTimer() {
    window.clearInterval(this.timer);
  }

  /**
   * Set a new interval
   */
  private setAutoplayTimer() {
    this.timer = window.setInterval(() => this.next(), 5000);
  }

  /**
   * When the component is destroyed we stop the carousel
   */
  ngOnDestroy() {
    this.clearAutoplayTimer();
  }
}
