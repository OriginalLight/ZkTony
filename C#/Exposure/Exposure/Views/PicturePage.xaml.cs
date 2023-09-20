using Exposure.Contracts.Services;
using Exposure.ViewModels;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace Exposure.Views;

public sealed partial class PicturePage : Page
{
    public PictureViewModel ViewModel
    {
        get;
    }

    public PicturePage()
    {
        ViewModel = App.GetService<PictureViewModel>();
        InitializeComponent();
    }

    private void Page_Loaded(object sender, RoutedEventArgs e)
    {
        var text = ViewModel.Folders.LastOrDefault();
        if (text != null)
        {
            AutoSuggestBox.Text = text;
        }
    }

    private async void TextChanged(AutoSuggestBox sender, AutoSuggestBoxTextChangedEventArgs args)
    {
        if (args.Reason != AutoSuggestionBoxTextChangeReason.UserInput)
        {
            return;
        }

        var suggestions = ViewModel.Folders.Where(p => p.StartsWith(sender.Text, StringComparison.OrdinalIgnoreCase)).ToList();

        if (suggestions.Count > 0)
        {
            sender.ItemsSource = suggestions;
        }
        else
        {
            sender.ItemsSource = new[] { "没有匹配项" };
        }

        await ViewModel.OnFolderChanged(sender.Text);
    }

    private async void QuerySubmitted(AutoSuggestBox sender, AutoSuggestBoxQuerySubmittedEventArgs args)
    {
        await ViewModel.OnFolderChanged(args.QueryText);
    }

    private async void SuggestionChosen(AutoSuggestBox sender, AutoSuggestBoxSuggestionChosenEventArgs args)
    {
        await ViewModel.OnFolderChanged(sender.Text);
    }

    private async void DateChanged(CalendarDatePicker sender, CalendarDatePickerDateChangedEventArgs args)
    {
        var date = sender.Date;
        if (date == null)
        {
            return;
        }
        var text = date.Value.ToString("yyyy-MM-dd");
        AutoSuggestBox.Text = text;
        await ViewModel.OnFolderChanged(text);
    }

    private void OnItemClick(object sender, ItemClickEventArgs e)
    {
        if (e.ClickedItem == null)
        {
            return;
        }
        var navigationService = App.GetService<INavigationService>();
        navigationService.SetListDataItemForNextConnectedAnimation(e.ClickedItem);
        navigationService.NavigateTo(typeof(PictureDetailViewModel).FullName!, e.ClickedItem);
    }
}
